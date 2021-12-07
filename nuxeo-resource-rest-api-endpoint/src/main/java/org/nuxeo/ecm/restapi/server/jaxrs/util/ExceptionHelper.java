/*
 * (C) Copyright 2021 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Michael Vachette
 */

package org.nuxeo.ecm.restapi.server.jaxrs.util;

import org.nuxeo.ecm.automation.OperationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

public class ExceptionHelper {

    public static String findErrorMessage(Exception e) {
        OperationException operationException = null;
        Throwable currentException = e;

        //find the original OperationException. There may be several levels depending on the number of automation chain / script nested calls
        do {
            if (currentException instanceof OperationException) {
                operationException = (OperationException) currentException;
            }
            if (currentException instanceof UndeclaredThrowableException) {
                currentException = ((UndeclaredThrowableException)currentException).getUndeclaredThrowable();
            } else if (currentException instanceof InvocationTargetException) {
                currentException = ((InvocationTargetException) currentException).getTargetException();
            } else {
                currentException = currentException.getCause();
            }
        } while (currentException != null);

        //get the script exception message if any
        if (operationException != null) {
            Throwable automationCause = operationException.getCause();
            if (automationCause instanceof UndeclaredThrowableException) {
                Throwable t = ((UndeclaredThrowableException)automationCause).getUndeclaredThrowable();
                if (t instanceof InvocationTargetException) {
                    Throwable target = ((InvocationTargetException)t).getTargetException();
                    return target.getMessage();
                }
            }
        }
        return null;
    }
}
