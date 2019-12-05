package Environment.IO_Package;

public enum Enum_IO {

    OPENED, // File was opened successfully
    CLOSED, // File was closed successfully
    DELETED, // File was deleted successfully
    WROTE_SUCCESSFULLY, // Text was written successfully
    CURRENT_FILE_STILL_OPEN, // File is used,
    INVALID_PATH, // Wrong path was given,
    ERROR; // Any unexpected error

}
