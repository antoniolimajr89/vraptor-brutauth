package br.com.caelum.brutal.brutauth.auth.rules;


public interface SimpleBrutauthRule extends BrutauthRule {
	boolean isAllowed(long accessLevel);
}
