package br.com.caelum.brutauth.reflection;


import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Test;

import br.com.caelum.brutauth.auth.rules.CustomBrutauthRule;
import br.com.caelum.brutauth.reflection.BrutauthMethod;

public class BrutauthMethodTest {

	@Test
	public void should_not_throw_exception_if_method_is_defined() throws NoSuchMethodException, SecurityException {
		Method realMethod = RightFakeRule.class.getMethod("isAllowed", RightFakeObject.class);
		Object[] arguments = fakeArgs(new RightFakeObject());
		BrutauthMethod brutauthMethod = new BrutauthMethod(arguments, realMethod, new RightFakeRule());
		assertTrue(brutauthMethod.invoke());
	}
	
	@Test(expected=NoSuchMethodException.class)
	public void should_throw_exception_if_method_is_not_defined() throws NoSuchMethodException, SecurityException {
		Method realMethod = RightFakeRule.class.getMethod("isAllowed", RightFakeObject.class);
		Object[] arguments = fakeArgs(new FalseFakeObject());
		new BrutauthMethod(arguments, realMethod, new RightFakeRule());
	}
	
	@Test
	public void should_invoke_method_with_varargs() throws NoSuchMethodException, SecurityException {
		Method realMethod = RightFakeRule.class.getMethod("isAllowed", Object[].class);
		Object[] arguments = fakeArgs(fakeArgs(new FalseFakeObject()));
		BrutauthMethod brutauthMethod = new BrutauthMethod(arguments, realMethod, new RightFakeRule());
		assertTrue(brutauthMethod.invoke());
	}
	
	@Test
	public void should_find_method_without_arguments() throws NoSuchMethodException, SecurityException {
		Method realMethod = FakeRuleWithoutArguments.class.getMethod("isAllowed");
		BrutauthMethod brutauthMethod = new BrutauthMethod(null, realMethod, new FakeRuleWithoutArguments());
		assertTrue(brutauthMethod.invoke());
	}
	
	private Object[] fakeArgs(Object rule) {
		return new Object[]{rule};
	}

	public class RightFakeRule implements CustomBrutauthRule{
		public boolean isAllowed(RightFakeObject fake){
			return true;
		}

		public boolean isAllowed(Object...args){
			return true;
		}
	}
	
	public class FakeRuleWithoutArguments implements CustomBrutauthRule{
		public boolean isAllowed(RightFakeObject fake){
			return true;
		}
		
		public boolean isAllowed(){
			return true;
		}
	}
	
	public class RightFakeObject implements CustomBrutauthRule{
	}

	public class FalseFakeObject implements CustomBrutauthRule{
	}

}
