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

INSERT INTO devil_fruit_type_version (version_id, romaji)
SELECT version_id, word || ' Gomu Mera No Mi ' || n
FROM perf_seed;

INSERT INTO devil_fruit_type_version_translation (version_id, language_code, name, description, advantages,
                                                  disadvantages)
SELECT version_id, 'en', english || ' fruit ' || n,
       'Eating it grants the power of ' || lower(english) || '. ' || repeat('Lorem ipsum dolor sit amet. ', 20),
       repeat('A strong advantage. ', 8), repeat('A weakness to the sea. ', 6)
FROM perf_seed
UNION ALL
SELECT version_id, 'it', 'Frutto ' || lower(italian) || ' ' || n,
       'Mangiarlo dona il potere di ' || lower(italian) || '. ' || repeat('Lorem ipsum dolor sit amet. ', 20),
       repeat('Un grande vantaggio. ', 8), repeat('Una debolezza verso il mare. ', 6)
FROM perf_seed;

INSERT INTO content_slug (entity_type, slug, content_id, assigned_at)
SELECT 'DEVIL_FRUIT_TYPE', slug_of(word || ' Gomu Mera No Mi ' || n), content_id, now()
FROM perf_seed;

COMMIT;

ANALYZE content, content_version, devil_fruit_type_version, devil_fruit_type_version_translation, content_slug;
SELECT count(*) AS contents_online FROM published.devil_fruit_type WHERE language = 'en';
