/**
 * Copyright (c) Connexta
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package com.connexta.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

/** Marks an exception class to provide additional details about the error. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DetailedResponseStatus {
  /** Alias for {@link #code}. */
  @AliasFor("code")
  int value() default -1;

  /**
   * Specifies a more specific code for the error that should be included in the resulting error
   * message (defaults to <code>-1</code>).
   *
   * @return a more specific code for the error or <code>-1</code> if no specific code is available
   */
  @AliasFor("value")
  int code() default -1;

  /**
   * Specifies additional details information about the error that should be included in the
   * resulting error message (defaults to none).
   *
   * @return additional information about the error
   */
  String[] details() default {};
}
