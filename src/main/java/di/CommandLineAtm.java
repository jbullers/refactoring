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
package di;

import static java.nio.charset.StandardCharsets.UTF_8;

import di.command.Command;
import di.command.CommandProcessor;
import di.command.CommandProcessorFactory;
import di.db.InMemoryDatabase;
import java.math.BigDecimal;
import java.util.Scanner;

/**
 * Main class for the command-line ATM.
 */
class CommandLineAtm {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, UTF_8.name());
        CommandProcessor commandProcessor =
              CommandProcessorFactory.builder()
                                     .minimumBalance(BigDecimal.ZERO)
                                     .maximumWithdrawl(new BigDecimal(1000))
                                     .database(new InMemoryDatabase())
                                     .outputter(System.out::println)
                                     .build()
                                     .commandProcessor();

        while (scanner.hasNextLine()) {
            Command.Status commandStatus = commandProcessor.process(scanner.nextLine());
            if (commandStatus.equals(Command.Status.INPUT_COMPLETED)) {
                break;
            }
        }
    }
}
