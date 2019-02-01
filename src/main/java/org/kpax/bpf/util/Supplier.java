/*******************************************************************************
 * Copyright (c) 2018 Eugen Covaci.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * Contributors:
 *     Eugen Covaci - initial design and implementation
 *******************************************************************************/

package org.kpax.bpf.util;

/**
 * A special kind of {@link java.util.function.Supplier} who's {@link #get()} method throws {@link Exception}
 *
 * @param <T>
 * @author Eugen Covaci
 */
@FunctionalInterface
public interface Supplier<T> {

    /**
     * Gets the result.
     *
     * @return The result.
     * @throws Exception
     */
    T get() throws Exception;
}
