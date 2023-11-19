/*
 * Copyright (C) 2019 The Dagger Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package di.command;

import di.output.Outputter;
import java.util.List;
import javax.inject.Inject;

final class HelloWorldCommand implements Command {
  private final Outputter outputter;

  @Inject
  HelloWorldCommand(Outputter outputter) {
    this.outputter = outputter;
  }

  @Override
  public Result handleInput(List<String> args) {
    if (!args.isEmpty()) {
      return Result.invalid();
    }
    outputter.output("howdy!");
    return Result.handled();
  }
}
