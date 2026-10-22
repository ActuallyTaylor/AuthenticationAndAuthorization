package org.tactics.authenticationauthorization.cli;

import javax.security.auth.callback.*;
import java.io.Console;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class ConsoleCallbackHandler implements CallbackHandler {

  @Override
  public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    Console console = System.console();
    for (Callback callback : callbacks) {
      if (callback instanceof NameCallback nameCallback) {
        nameCallback.setName(console.readLine(nameCallback.getPrompt()));
      } else if (callback instanceof PasswordCallback passwordCallback) {
        passwordCallback.setPassword(console.readPassword(passwordCallback.getPrompt()));
      } else if (callback instanceof ChoiceCallback choiceCallback) {
        String[] opts = choiceCallback.getChoices();
        console.printf("%s%n", choiceCallback.getPrompt());

        for (int i = 0; i < opts.length; i++) {
          System.out.printf("\t%d) %s%n", i, opts[i]);
        }

        String line = console.readLine("Choice [default: %d]: ", choiceCallback.getDefaultChoice());
        int index = line.isBlank() ? choiceCallback.getDefaultChoice() : Integer.parseInt(line.trim());
        choiceCallback.setSelectedIndex(index);
      } else if (callback instanceof TextInputCallback) {
        TextInputCallback tic = (TextInputCallback) callback;

        // Print the prompt
        System.out.print(tic.getPrompt());
        System.out.flush();

        // Read the string from the user
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String userInput = reader.readLine();

        // Set the text value back into the callback
        tic.setText(userInput != null ? userInput.trim() : "");
      } else {
        throw new UnsupportedCallbackException(callback);
      }
    }
  }
}
