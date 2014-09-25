package br.com.caelum.brutauth.interceptors;

import static br.com.caelum.brutauth.util.TestUtils.method;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.caelum.brutauth.auth.rules.CustomBrutauthRule;
import br.com.caelum.brutauth.verifier.BrutauthRulesVerifiers;
import br.com.caelum.vraptor.controller.ControllerMethod;
import br.com.caelum.vraptor.core.InterceptorStack;

@RunWith(MockitoJUnitRunner.class)
public class BrutauthRuleInterceptorTest {

	@Mock
	private BrutauthRulesVerifiers verifiers;
	@Mock
	private InterceptorStack stack;
	
	private MyController controller;
	private BrutauthRuleInterceptor interceptor;
	private ControllerMethod singleRuleControllerMethod;

	@Before
	public void setUp() throws Exception {
		controller = new MyController();
		singleRuleControllerMethod = method(MyController.class, "myCustomRuleMethod");
		interceptor = new BrutauthRuleInterceptor(verifiers);
	}
	
	@Test
	public void should_stop_stack_if_rule_says_so() throws Exception {
		when(verifiers.verify(Mockito.any(BrutauthClassOrMethod.class))).thenReturn(false);
		
		assertTrue("should accept myCustomRuleMethod", interceptor.accepts(singleRuleControllerMethod));
		interceptor.intercept(stack, singleRuleControllerMethod, controller);

		verify(stack, never()).next(singleRuleControllerMethod, controller);
	}
	
	@Test
	public void should_continue_stack_if_rule_allows_access() throws Exception {
		when(verifiers.verify(Mockito.any(BrutauthClassOrMethod.class))).thenReturn(true);

		assertTrue("should accept myCustomRuleMethod", interceptor.accepts(singleRuleControllerMethod));
		interceptor.intercept(stack, singleRuleControllerMethod, controller);

		verify(stack).next(singleRuleControllerMethod, controller);
	}

	@Test
	public void should_add_controllers_class_and_method_rules() throws Exception {
		when(verifiers.verify(Mockito.any(BrutauthClassOrMethod.class))).thenReturn(true);
		ControllerMethod controllerWithRulesMethod = method(ControllerWithRules.class, "methodWithRules");
		
		assertTrue("should accept ControllerWithRules", interceptor.accepts(controllerWithRulesMethod));
		interceptor.intercept(stack, controllerWithRulesMethod, controller);
		
		verify(verifiers, times(2)).verify(any(BrutauthClassOrMethod.class));
	}
	
	public class TrueCustomRule implements CustomBrutauthRule{
		public boolean isAllowed(String string) {
			return true;
		}
	}
}
