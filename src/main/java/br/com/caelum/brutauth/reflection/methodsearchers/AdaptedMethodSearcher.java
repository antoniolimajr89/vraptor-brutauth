package br.com.caelum.brutauth.reflection.methodsearchers;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import br.com.caelum.brutauth.auth.rules.CustomBrutauthRule;
import br.com.caelum.brutauth.reflection.Argument;
import br.com.caelum.brutauth.reflection.BrutauthMethod;
import br.com.caelum.brutauth.reflection.NamedParametersMethod;
import br.com.caelum.vraptor.http.Parameter;

@Dependent
public class AdaptedMethodSearcher implements MethodSearcher {

	private final DefaultMethodSearcher defaultMethodSearcher;
	private final ArgumentParameterMatcher matcher;

	@Inject
	public AdaptedMethodSearcher(DefaultMethodSearcher defaultMethodSearcher, ArgumentParameterMatcher matcher) {
		this.defaultMethodSearcher = defaultMethodSearcher;
		this.matcher = matcher;
	}
	
	@Override
	public BrutauthMethod search(CustomBrutauthRule ruleToSearch, Argument...arguments) {
		try {
			NamedParametersMethod defaultMethod = defaultMethodSearcher.getMethod(ruleToSearch);
			Parameter[] parameters = defaultMethod.getParameters();

			Argument[] matchedArguments = matcher.getValuesMatchingParameters(parameters, arguments);
			return new BrutauthMethod(matchedArguments, defaultMethod.getMethod(), ruleToSearch);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}
}
