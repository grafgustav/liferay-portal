/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.apio.architect.impl.routes;

import static com.liferay.apio.architect.impl.routes.RoutesBuilderUtil.provide;

import com.liferay.apio.architect.alias.IdentifierFunction;
import com.liferay.apio.architect.alias.form.FormBuilderFunction;
import com.liferay.apio.architect.alias.routes.NestedCreateItemFunction;
import com.liferay.apio.architect.alias.routes.NestedGetPageFunction;
import com.liferay.apio.architect.alias.routes.permission.HasNestedAddingPermissionFunction;
import com.liferay.apio.architect.credentials.Credentials;
import com.liferay.apio.architect.form.Form;
import com.liferay.apio.architect.function.throwable.ThrowableBiFunction;
import com.liferay.apio.architect.function.throwable.ThrowableHexaFunction;
import com.liferay.apio.architect.function.throwable.ThrowablePentaFunction;
import com.liferay.apio.architect.function.throwable.ThrowableTetraFunction;
import com.liferay.apio.architect.function.throwable.ThrowableTriFunction;
import com.liferay.apio.architect.functional.Try;
import com.liferay.apio.architect.impl.alias.ProvideFunction;
import com.liferay.apio.architect.impl.form.FormImpl;
import com.liferay.apio.architect.impl.operation.CreateOperation;
import com.liferay.apio.architect.impl.pagination.PageImpl;
import com.liferay.apio.architect.impl.single.model.SingleModelImpl;
import com.liferay.apio.architect.operation.Operation;
import com.liferay.apio.architect.pagination.PageItems;
import com.liferay.apio.architect.pagination.Pagination;
import com.liferay.apio.architect.routes.NestedCollectionRoutes;
import com.liferay.apio.architect.uri.Path;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Alejandro Hernández
 */
public class NestedCollectionRoutesImpl<T, S, U>
	implements NestedCollectionRoutes<T, S, U> {

	public NestedCollectionRoutesImpl(BuilderImpl<T, S, U> builderImpl) {
		_form = builderImpl._form;
		_nestedCreateItemFunction = builderImpl._nestedCreateItemFunction;
		_nestedGetPageFunction = builderImpl._nestedGetPageFunction;
	}

	@Override
	public Optional<Form> getFormOptional() {
		return Optional.ofNullable(_form);
	}

	@Override
	public Optional<NestedCreateItemFunction<T, U>>
		getNestedCreateItemFunctionOptional() {

		return Optional.ofNullable(_nestedCreateItemFunction);
	}

	@Override
	public Optional<NestedGetPageFunction<T, U>>
		getNestedGetPageFunctionOptional() {

		return Optional.ofNullable(_nestedGetPageFunction);
	}

	public static class BuilderImpl<T, S, U> implements Builder<T, S, U> {

		public BuilderImpl(
			String name, String nestedName, ProvideFunction provideFunction,
			Consumer<String> neededProviderConsumer,
			Function<Path, ?> pathToIdentifierFunction,
			Function<U, Optional<Path>> identifierToPathFunction) {

			_name = name;
			_nestedName = nestedName;
			_provideFunction = provideFunction;
			_neededProviderConsumer = neededProviderConsumer;

			_pathToIdentifierFunction = pathToIdentifierFunction::apply;
			_identifierToPathFunction = identifierToPathFunction;
		}

		@Override
		public <R> Builder<T, S, U> addCreator(
			ThrowableBiFunction<U, R, T> creatorThrowableBiFunction,
			HasNestedAddingPermissionFunction<U>
				hasNestedAddingPermissionFunction,
			FormBuilderFunction<R> formBuilderFunction) {

			_hasNestedAddingPermissionFunction =
				hasNestedAddingPermissionFunction;

			Form<R> form = formBuilderFunction.apply(
				new FormImpl.BuilderImpl<>(
					Arrays.asList("c", _name, _nestedName),
					_pathToIdentifierFunction));

			_form = form;

			_nestedCreateItemFunction =
				httpServletRequest -> identifier -> body -> Try.fromFallible(
					() -> creatorThrowableBiFunction.andThen(
						t -> new SingleModelImpl<>(
							t, _nestedName, Collections.emptyList())
					).apply(
						identifier, form.get(body)
					));

			return this;
		}

		@Override
		public <A, B, C, D, R> Builder<T, S, U> addCreator(
			ThrowableHexaFunction<U, R, A, B, C, D, T>
				creatorThrowableHexaFunction,
			Class<A> aClass, Class<B> bClass, Class<C> cClass, Class<D> dClass,
			HasNestedAddingPermissionFunction<U>
				hasNestedAddingPermissionFunction,
			FormBuilderFunction<R> formBuilderFunction) {

			_neededProviderConsumer.accept(aClass.getName());
			_neededProviderConsumer.accept(bClass.getName());
			_neededProviderConsumer.accept(cClass.getName());
			_neededProviderConsumer.accept(dClass.getName());

			_hasNestedAddingPermissionFunction =
				hasNestedAddingPermissionFunction;

			Form<R> form = formBuilderFunction.apply(
				new FormImpl.BuilderImpl<>(
					Arrays.asList("c", _name, _nestedName),
					_pathToIdentifierFunction));

			_form = form;

			_nestedCreateItemFunction =
				httpServletRequest -> identifier -> body -> provide(
					_provideFunction.apply(httpServletRequest), aClass, bClass,
					cClass, dClass,
					(a, b, c, d) -> creatorThrowableHexaFunction.andThen(
						t -> new SingleModelImpl<>(
							t, _nestedName, Collections.emptyList())
					).apply(
						identifier, form.get(body), a, b, c, d
					));

			return this;
		}

		@Override
		public <A, B, C, R> Builder<T, S, U> addCreator(
			ThrowablePentaFunction<U, R, A, B, C, T>
				creatorThrowablePentaFunction,
			Class<A> aClass, Class<B> bClass, Class<C> cClass,
			HasNestedAddingPermissionFunction<U>
				hasNestedAddingPermissionFunction,
			FormBuilderFunction<R> formBuilderFunction) {

			_neededProviderConsumer.accept(aClass.getName());
			_neededProviderConsumer.accept(bClass.getName());
			_neededProviderConsumer.accept(cClass.getName());

			_hasNestedAddingPermissionFunction =
				hasNestedAddingPermissionFunction;

			Form<R> form = formBuilderFunction.apply(
				new FormImpl.BuilderImpl<>(
					Arrays.asList("c", _name, _nestedName),
					_pathToIdentifierFunction));

			_form = form;

			_nestedCreateItemFunction =
				httpServletRequest -> identifier -> body -> provide(
					_provideFunction.apply(httpServletRequest), aClass, bClass,
					cClass,
					(a, b, c) -> creatorThrowablePentaFunction.andThen(
						t -> new SingleModelImpl<>(
							t, _nestedName, Collections.emptyList())
					).apply(
						identifier, form.get(body), a, b, c
					));

			return this;
		}

		@Override
		public <A, B, R> Builder<T, S, U> addCreator(
			ThrowableTetraFunction<U, R, A, B, T> creatorThrowableTetraFunction,
			Class<A> aClass, Class<B> bClass,
			HasNestedAddingPermissionFunction<U>
				hasNestedAddingPermissionFunction,
			FormBuilderFunction<R> formBuilderFunction) {

			_neededProviderConsumer.accept(aClass.getName());
			_neededProviderConsumer.accept(bClass.getName());

			_hasNestedAddingPermissionFunction =
				hasNestedAddingPermissionFunction;

			Form<R> form = formBuilderFunction.apply(
				new FormImpl.BuilderImpl<>(
					Arrays.asList("c", _name, _nestedName),
					_pathToIdentifierFunction));

			_form = form;

			_nestedCreateItemFunction =
				httpServletRequest -> identifier -> body -> provide(
					_provideFunction.apply(httpServletRequest), aClass, bClass,
					(a, b) -> creatorThrowableTetraFunction.andThen(
						t -> new SingleModelImpl<>(
							t, _nestedName, Collections.emptyList())
					).apply(
						identifier, form.get(body), a, b
					));

			return this;
		}

		@Override
		public <A, R> Builder<T, S, U> addCreator(
			ThrowableTriFunction<U, R, A, T> creatorThrowableTriFunction,
			Class<A> aClass,
			HasNestedAddingPermissionFunction<U>
				hasNestedAddingPermissionFunction,
			FormBuilderFunction<R> formBuilderFunction) {

			_neededProviderConsumer.accept(aClass.getName());

			_hasNestedAddingPermissionFunction =
				hasNestedAddingPermissionFunction;

			Form<R> form = formBuilderFunction.apply(
				new FormImpl.BuilderImpl<>(
					Arrays.asList("c", _name, _nestedName),
					_pathToIdentifierFunction));

			_form = form;

			_nestedCreateItemFunction =
				httpServletRequest -> identifier -> body -> provide(
					_provideFunction.apply(httpServletRequest), aClass,
					a -> creatorThrowableTriFunction.andThen(
						t -> new SingleModelImpl<>(
							t, _nestedName, Collections.emptyList())
					).apply(
						identifier, form.get(body), a
					));

			return this;
		}

		@Override
		public Builder<T, S, U> addGetter(
			ThrowableBiFunction<Pagination, U, PageItems<T>>
				getterThrowableBiFunction) {

			_nestedGetPageFunction =
				httpServletRequest -> path -> identifier -> provide(
					_provideFunction.apply(httpServletRequest),
					Pagination.class, Credentials.class,
					(pagination, credentials) ->
						getterThrowableBiFunction.andThen(
							items -> new PageImpl<>(
								_nestedName, items, pagination, path,
								_getOperations(credentials, identifier))
						).apply(
							pagination, identifier
						));

			return this;
		}

		@Override
		public <A, B, C, D> Builder<T, S, U> addGetter(
			ThrowableHexaFunction<Pagination, U, A, B, C, D, PageItems<T>>
				getterThrowableHexaFunction,
			Class<A> aClass, Class<B> bClass, Class<C> cClass,
			Class<D> dClass) {

			_neededProviderConsumer.accept(aClass.getName());
			_neededProviderConsumer.accept(bClass.getName());
			_neededProviderConsumer.accept(cClass.getName());
			_neededProviderConsumer.accept(dClass.getName());

			_nestedGetPageFunction =
				httpServletRequest -> path -> identifier -> provide(
					_provideFunction.apply(httpServletRequest),
					Pagination.class, aClass, bClass, cClass, dClass,
					Credentials.class,
					(pagination, a, b, c, d, credentials) ->
						getterThrowableHexaFunction.andThen(
							items -> new PageImpl<>(
								_nestedName, items, pagination, path,
								_getOperations(credentials, identifier))
						).apply(
							pagination, identifier, a, b, c, d
						));

			return this;
		}

		@Override
		public <A, B, C> Builder<T, S, U> addGetter(
			ThrowablePentaFunction<Pagination, U, A, B, C, PageItems<T>>
				getterThrowablePentaFunction,
			Class<A> aClass, Class<B> bClass, Class<C> cClass) {

			_neededProviderConsumer.accept(aClass.getName());
			_neededProviderConsumer.accept(bClass.getName());
			_neededProviderConsumer.accept(cClass.getName());

			_nestedGetPageFunction =
				httpServletRequest -> path -> identifier -> provide(
					_provideFunction.apply(httpServletRequest),
					Pagination.class, aClass, bClass, cClass, Credentials.class,
					(pagination, a, b, c, credentials) ->
						getterThrowablePentaFunction.andThen(
							items -> new PageImpl<>(
								_nestedName, items, pagination, path,
								_getOperations(credentials, identifier))
						).apply(
							pagination, identifier, a, b, c
						));

			return this;
		}

		@Override
		public <A, B> Builder<T, S, U> addGetter(
			ThrowableTetraFunction<Pagination, U, A, B, PageItems<T>>
				getterThrowableTetraFunction,
			Class<A> aClass, Class<B> bClass) {

			_neededProviderConsumer.accept(aClass.getName());
			_neededProviderConsumer.accept(bClass.getName());

			_nestedGetPageFunction =
				httpServletRequest -> path -> identifier -> provide(
					_provideFunction.apply(httpServletRequest),
					Pagination.class, aClass, bClass, Credentials.class,
					(pagination, a, b, credentials) ->
						getterThrowableTetraFunction.andThen(
							items -> new PageImpl<>(
								_nestedName, items, pagination, path,
								_getOperations(credentials, identifier))
						).apply(
							pagination, identifier, a, b
						));

			return this;
		}

		@Override
		public <A> Builder<T, S, U> addGetter(
			ThrowableTriFunction<Pagination, U, A, PageItems<T>>
				getterThrowableTriFunction,
			Class<A> aClass) {

			_neededProviderConsumer.accept(aClass.getName());

			_nestedGetPageFunction =
				httpServletRequest -> path -> identifier -> provide(
					_provideFunction.apply(httpServletRequest),
					Pagination.class, aClass, Credentials.class,
					(pagination, a, credentials) ->
						getterThrowableTriFunction.andThen(
							items -> new PageImpl<>(
								_nestedName, items, pagination, path,
								_getOperations(credentials, identifier))
						).apply(
							pagination, identifier, a
						));

			return this;
		}

		@Override
		public NestedCollectionRoutes<T, S, U> build() {
			return new NestedCollectionRoutesImpl<>(this);
		}

		private List<Operation> _getOperations(
			Credentials credentials, U identifier) {

			Optional<Path> optional = _identifierToPathFunction.apply(
				identifier);

			if (!optional.isPresent()) {
				return Collections.emptyList();
			}

			Path path = optional.get();

			Boolean canAdd = Try.fromFallible(
				() -> _hasNestedAddingPermissionFunction.apply(
					credentials, identifier)
			).orElse(
				false
			);

			if (!canAdd) {
				return Collections.emptyList();
			}

			CreateOperation createOperation = new CreateOperation(
				_form, _name + "/" + _nestedName,
				path.asURI() + "/" + _nestedName);

			return Collections.singletonList(createOperation);
		}

		private Form _form;
		private ThrowableBiFunction<Credentials, U, Boolean>
			_hasNestedAddingPermissionFunction;
		private final Function<U, Optional<Path>> _identifierToPathFunction;
		private final String _name;
		private final Consumer<String> _neededProviderConsumer;
		private NestedCreateItemFunction<T, U> _nestedCreateItemFunction;
		private NestedGetPageFunction<T, U> _nestedGetPageFunction;
		private final String _nestedName;
		private final IdentifierFunction<?> _pathToIdentifierFunction;
		private final ProvideFunction _provideFunction;

	}

	private final Form _form;
	private final NestedCreateItemFunction<T, U> _nestedCreateItemFunction;
	private final NestedGetPageFunction<T, U> _nestedGetPageFunction;

}