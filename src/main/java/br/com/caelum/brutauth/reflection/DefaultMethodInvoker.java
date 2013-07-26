package br.com.caelum.brutauth.reflection;

import br.com.caelum.brutauth.auth.rules.CustomBrutauthRule;
import br.com.caelum.brutauth.reflection.methodsearchers.MethodSearchers;
import br.com.caelum.vraptor.ioc.Component;

@Component
public class DefaultMethodInvoker {

		private final MethodSearchers searcher;

		public DefaultMethodInvoker(MethodSearchers searcher) {
			this.searcher = searcher;
		}
	
		public boolean invoke(CustomBrutauthRule toInvoke, Object[] args) {
			return searcher.search(toInvoke, args).invoke();
		}

}
