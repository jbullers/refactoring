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

package di.command.user;

import dagger.BindsInstance;
import dagger.Module;
import dagger.Subcomponent;
import di.PerSession;
import di.Username;
import di.command.CommandRouter;
import di.db.AccountModule;

@PerSession
@Subcomponent(modules = { AccountModule.class, UserCommandsModule.class})
public interface UserCommandsRouter {

  CommandRouter router();

  @Subcomponent.Factory
  interface Factory {
    UserCommandsRouter create(
          @BindsInstance @Username String username);
  }

  @Module(subcomponents = UserCommandsRouter.class)
  interface InstallationModule {}
}