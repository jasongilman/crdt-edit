# crdt-edit

An experimental collaborative text editor built on CRDTs

## TODOs

  * Nice To haves
    * Use persistent http connections
    * Add text area showing the current logoot document.

## Usage

### REPL

  * Run `lein repl`
  * Enter `(reset)`
    * This will start two instances of the editor within one repl that will be connected to one another.

### Command Line

  * Build it
    * `lein uberjar`
  * Run it
    * `java -jar target/crdt-edit-0.1.0-SNAPSHOT-standalone.jar --port 3000 --site alpha`
  * Enter the hostname and port of another collaborator on the local network or you can start up an additional instance for testing on a different port.

## License

Copyright © 2014 Jason Gilman

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
