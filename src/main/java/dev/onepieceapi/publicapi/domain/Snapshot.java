package dev.onepieceapi.publicapi.domain;

/**
 * What a read of the published content finds, as of one published revision: the caller
 * already has it ({@link NotModified}), or the value read ({@link Fresh}).
 */
public sealed interface Snapshot<T> {

	long revision();

	record NotModified<T>(long revision) implements Snapshot<T> {

	}

	record Fresh<T>(long revision, T value) implements Snapshot<T> {

	}

}
