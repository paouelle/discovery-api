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
package com.connexta.discovery.rest.springboot.mock;

import java.net.URI;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Size;

public class URISizeValidator implements ConstraintValidator<Size, URI> {
  private int min;
  private int max;

  @Override
  public void initialize(Size parameters) {
    this.min = parameters.min();
    this.max = parameters.max();
    this.validateParameters();
  }

  @Override
  public boolean isValid(URI uri, ConstraintValidatorContext cxt) {
    if (uri == null) {
      return true;
    }
    final int length = uri.toString().length();

    return length >= this.min && length <= this.max;
  }

  private void validateParameters() {
    if (this.min < 0) {
      throw new IllegalArgumentException("The min parameter cannot be negative");
    } else if (this.max < 0) {
      throw new IllegalArgumentException("The max parameter cannot be negative");
    } else if (this.max < this.min) {
      throw new IllegalArgumentException("The length cannot be negative");
    }
  }
}
