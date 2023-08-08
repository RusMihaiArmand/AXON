package ro.axon.dot.security;

public class TokenException extends Exception {

  public TokenException(String token, String message) {
    super(String.format("Failed for [%s]: %s", token, message));
  }
}
