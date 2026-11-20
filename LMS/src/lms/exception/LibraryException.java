/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lms.exception;

/**
 *
 * @author Mary
 */
public class LibraryException extends Exception{
    public LibraryException(String message) {
        super(message);
    }

    public LibraryException(
            String message, Throwable cause) {
        super(message, cause);
    }
}
