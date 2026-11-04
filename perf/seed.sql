-- Synthetic dataset for the performance baseline (plan D16): :count Devil Fruit Types online,
-- each written in Italian and English, written as content-service's owner role. Idempotent:
-- the previous run's rows (author 'perf-seed') are removed first. Local cluster only.
-- Usage: scripts/perf-seed.sh [count]

\set ON_ERROR_STOP on
\if :{?count}
\else
\set count 5000
\endif

BEGIN;

DELETE FROM content WHERE id IN (SELECT content_id FROM content_version WHERE author_username = 'perf-seed');

CREATE TEMP TABLE perf_seed AS
SELECT n,
       gen_random_uuid() AS content_id,
       gen_random_uuid() AS version_id,
       (ARRAY ['Gomu', 'Mera', 'Hie', 'Goro', 'Suna', 'Yami', 'Pika', 'Magu', 'Nikyu', 'Hana', 'Bara', 'Kilo', 'Doru',
               'Ito', 'Mori', 'Sabi', 'Toge', 'Buki', 'Noro', 'Horo'])[1 + n % 20] AS word,
       (ARRAY ['Fire', 'Ice', 'Rubber', 'Sand', 'Shadow', 'Light', 'Magma', 'Flower', 'Thorn', 'Weapon'])[1 + n % 10]
           AS english,
       (ARRAY ['Fuoco', 'Ghiaccio', 'Gomma', 'Sabbia', 'Ombra', 'Luce', 'Magma', 'Fiore', 'Spina', 'Arma'])[1 + n % 10]
           AS italian
FROM generate_series(1, :count) AS n;

INSERT INTO content (id, entity_type, created_at)
SELECT content_id, 'DEVIL_FRUIT_TYPE', now()
FROM perf_seed;

INSERT INTO content_version (id, content_id, version_number, author_user_id, author_username, author_email, status,
                             created_at, updated_at)
SELECT version_id, content_id, 1, gen_random_uuid(), 'perf-seed', 'perf-seed@example.org', 'PUBLISHED',
       now() - make_interval(secs => n), now() - make_interval(secs => n)
FROM perf_seed;

-- Every type first appears in the earliest published chapter, so that a reader-position filter at or
-- after it (asOfChapter) keeps the whole dataset visible. Needs at least one published chapter.
INSERT INTO devil_fruit_type_version (version_id, romaji, first_appearance_chapter_id)
SELECT version_id, word || ' Gomu Mera No Mi ' || n,
       (SELECT id FROM published.chapter ORDER BY number LIMIT 1)
FROM perf_seed;

INSERT INTO devil_fruit_type_version_translation (version_id, language_code, name)
SELECT version_id, 'en', english || ' fruit ' || n FROM perf_seed
UNION ALL
SELECT version_id, 'it', 'Frutto ' || lower(italian) || ' ' || n FROM perf_seed;

-- One paragraph per section (description, advantages, disadvantages), written in both languages.
CREATE TEMP TABLE perf_paragraph AS
SELECT gen_random_uuid() AS paragraph_id, s.version_id, section.name AS section, section.position
FROM perf_seed s
CROSS JOIN (VALUES ('DESCRIPTION', 0), ('ADVANTAGES', 1), ('DISADVANTAGES', 2)) AS section (name, position);

INSERT INTO devil_fruit_type_version_paragraph (id, version_id, position, section)
SELECT paragraph_id, version_id, position, section FROM perf_paragraph;

INSERT INTO devil_fruit_type_version_paragraph_translation (paragraph_id, language_code, text)
SELECT p.paragraph_id, 'en',
       CASE p.section
           WHEN 'DESCRIPTION' THEN 'Eating it grants the power of ' || lower(s.english) || '. ' || repeat('Lorem ipsum dolor sit amet. ', 20)
           WHEN 'ADVANTAGES' THEN repeat('A strong advantage. ', 8)
           ELSE repeat('A weakness to the sea. ', 6) END
FROM perf_paragraph p JOIN perf_seed s USING (version_id)
UNION ALL
SELECT p.paragraph_id, 'it',
       CASE p.section
           WHEN 'DESCRIPTION' THEN 'Mangiarlo dona il potere di ' || lower(s.italian) || '. ' || repeat('Lorem ipsum dolor sit amet. ', 20)
           WHEN 'ADVANTAGES' THEN repeat('Un grande vantaggio. ', 8)
           ELSE repeat('Una debolezza verso il mare. ', 6) END
FROM perf_paragraph p JOIN perf_seed s USING (version_id);

INSERT INTO content_slug (entity_type, slug, content_id, assigned_at)
SELECT 'DEVIL_FRUIT_TYPE', slug_of(word || ' Gomu Mera No Mi ' || n), content_id, now()
FROM perf_seed;

COMMIT;

ANALYZE content, content_version, devil_fruit_type_version, devil_fruit_type_version_translation,
        devil_fruit_type_version_paragraph, devil_fruit_type_version_paragraph_translation, content_slug;
SELECT count(*) AS contents_online FROM published.devil_fruit_type WHERE language = 'en';
