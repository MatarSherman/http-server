# HTTP Server

This is a personal project, it's a lightweight, non-blocking HTTP server built from scratch, in Java. It's designed to be a simple, educational, and performant server that handles basic HTTP requests and responses.

## Features

*   **Efficient Concurrency:** By using a virtual thread for each new socket connection.
*   **Customizable Serialization:** Supports various different response body serialization strategies.
*   **Easy to Use:** The server can be set up and started with just a few lines of code.

## Local Deployment

### Prerequisites

*   Java 21 or higher
*   Gradle

### Running the Server

1.  Clone the repository:
    ```bash
    git clone https://github.com/your-username/http-server.git
    ```
2.  Navigate to the project directory:
    ```bash
    cd http-server
    ```
3.  Build the execution file
    ```bash
    # MacOS/Linux:
    ./gradlew installDist
    ```
    ```bash
    # Windows
     gradlew.bat installDist
    ```
4. Run the execution file
    ```bash
    # MacOS/Linux:
    ./build/install/http-server/bin/http-server
    ```
    ```bash
    # Windows
    ./build/install/http-server/bin/http-server.bat
    ```
For now, as an in-progress project, it's configured to run the Main.java file in src/main/java/dev.matar.httpserver.

For developers running locally, clone the project into your preferred IDE, and run like any other Gradle project.

The server will start on port `8765` by default. You can change the port in the `Main.java` file.

## Contributing

Contributions are welcome! If you have any ideas, suggestions, or bug reports, please open an issue or submit a pull request.
