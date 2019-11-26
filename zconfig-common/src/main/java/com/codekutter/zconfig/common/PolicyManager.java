package com.codekutter.zconfig.common;

/**
 * Interface to define Authorisation Policy Managers.
 *
 * @param <T> - Entity Type
 * @param <U> - User Profile
 * @param <O> - Operation Requested
 */
public interface PolicyManager<T, U, O> {
    /**
     * Check if the passed user is authorised to perform the requested operation
     * on the specified entity.
     *
     * @param entity - Target entity
     * @param user - Requesting User
     * @param operation - Requested operation
     * @return - Is authorized?
     * @throws PolicyException - Exception will be raised if authorisation call has errors.
     */
    boolean checkAuthorised(T entity, U user, O operation) throws PolicyException;
}
