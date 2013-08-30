VRaptor Brutauth
================

VRaptor Brutauth provides an easy way to verify permission to access(authorization) a specific action nor controller when using VRaptor.

##How to Install?

You can download vraptor-brutauth.jar from maven repository or configured in any compatible tool: 

```
<dependency>
	<groupId>br.com.caelum.vraptor</groupId>
	<artifactId>vraptor-brutauth</artifactId>
	<version>1.0.0</version>
</dependency>
```

##How to Use?

VRaptor Brutauth suports two authentication rule types: simple and custom.

###Simple Rules

Simple Rules are the ones that uses only an access level to authorize.

####How to create Simple Rules?

You only need to create a class that implements `SimpleBrutauthRule` and annotate it with VRaptor's `@Component`.

e.g.:

```
import br.com.caelum.brutauth.auth.rules.SimpleBrutauthRule;
import br.com.caelum.vraptor.ioc.Component;

@Component
public class CanAccess implements SimpleBrutauthRule {

	private UserSession userSession;

	public CanAccess(UserSession userSession) {
		this.userSession = userSession;
	}

	@Override
	public boolean isAllowed(long accessLevel) {
		return userSession.getUser().hasAccessLevel(accessLevel);
	}

}

```

####How to use my rule?

Annotate your action with `@SimpleBrutauthRules`, passing as argument your rule's class:

```
@Resource
public class BrutauthController {

	@SimpleBrutauthRules(CanAccess.class)
	public void somePage(){
		//logic
	}
}

```

To define the necessary accessLevel to your rule, annotate the action with `@AccessLevel`, with the necessary value.

The result will be like:

```

@Resource
public class BrutauthController {

	@SimpleBrutauthRules(CanAccess.class)
	@AccessLevel(2000)
	public void somePage(){
		//logic
	}
}

```

###Custom Rules

The VRaptor Brutauth also has the Custom Rules feature. The difference between the custom and simple rules is that with the custom ones, you can recieve as arguments anything that your action recieves.

e.g.
Consider an action which recieves an argument of type `Car`:

```
@Resource
public class BrutauthController {
	public void showCar(Car car){
		//logic
	}
}
```

You will be able to recieve the same `Car` in your `isAllowed` method.

####How to create custom rules?

Just create a class that implements `CustomBrutauthRule`, with the VRaptor's `@Component` annotation.

```
import br.com.caelum.brutauth.auth.rules.CustomBrutauthRule;
import br.com.caelum.vraptor.ioc.Component;

@Component
public class CanAccessCar implements CustomBrutauthRule {

	private UserSession userSession;

	public CanAccessCar(UserSession userSession) {
		this.userSession = userSession;
	}

	public boolean isAllowed(Car car) {
		return userSession.getUser().canAccess(car);
	}
}
```

By default, the method name should be `isAllowed` but, if you want to, you can use annother name annotating the method with `@BrutauthValidation`

```
import br.com.caelum.brutauth.auth.rules.CustomBrutauthRule;
import br.com.caelum.vraptor.ioc.Component;

@Component
public class CanAccessCar implements CustomBrutauthRule {

	private UserSession userSession;

	public CanAccessCar(UserSession userSession) {
		this.userSession = userSession;
	}

	@BrutauthValidation
	public boolean otherName(Car car) {
		return userSession.getUser().canAccess(car);
	}
}
```
Don't forget to annotate your action with `@CustomBrutauthRules(CanAccessCar.class)`:

```
@Resource
public class BrutauthController {

	@CustomBrutauthRules(CanAccessCar.class)
	public void showCar(Car car){
		//logic
	}
}
```

###What if I need to limit the access of an entire controller?

To do this, you do not need to annotate each method with the required rule. You just need to annotate the controller:

```
@Resource
@SimpleBrutauthRules(CanAccess.class)
public class BrutauthController {
	public void somePage(){
		//logic
	}

	public void otherPage(){
		//logic
	}
}
```
Doing this, to access anyone of the methods in that controller, the rule `CanAccess.class` needs to be satisfied.

It also works with `@CustomBrutauthRules`:

```
@Resource
@CustomBrutauthRules(CanAccessCar.class)
public class CarController {
	public void showCar(Car car){
		//logic
	}

	public void editCar(Car car){
		//logic
	}
}
```

###How to customize the framework behaviour when isAllowed returns false?

By default, the VRaptor Brutauth will always return status `403` when a rule return false. To customize this behaviour, you must create a class that implements `RuleHandler`.

e.g.:

If you want the framework to redirect the user to the login form when a rule fails. Your `RuleHandler` would be something like:

```
@Component
public class LoggedHandler implements RuleHandler{
	private final Result result;

	public LoggedHandler(Result result) {
		this.result = result;
	}

	@Override
	public void handle() {
		result.redirectTo(AuthController.class).loginForm();
	}
}

```
Now you just need to add the `@HandledBy(LoggedHandler.class)` annotation to your rule:

```
@Component
@HandledBy(LoggedHandler.class)
public class LoggedAccessRule implements CustomBrutauthRule {

	private UserSession userSession;

	public LoggedAccessRule(UserSession userSession) {
		this.userSession = userSession;
	}

	public boolean isAllowed() {
		return userSession.isLogged();
	}
}
```
And your `RuleHandler` will be invoked when that rule fails.

###Using a different RuleHandler at a specific action

You can add the same `@HandledBy` annotation to your controller action. It will overwrite your rule's handler:

e.g.:

Suppose the following action:

```
@Resource
public class BrutauthController {
	@CustomBrutauthRules(LoggedAccessRule.class)
	@HandledBy(OtherHandler.class)
	public void showCar(Car car){
		//logic
	}
}
```
The `RuleHandler` to be used will be the `OtherHandler`, even if your rule contains other `@HandledBy` annotation.

###Many rules in an action

If you pass an array as argument to both `@CustomBrutauthRules` nor `@SimpleBrutauthRules`, all of then will be evaluated:

```
@Resource
public class BrutauthController {
	@CustomBrutauthRules({LoggedAccessRule.class, CanAccessCar.class})
	public void showCar(Car car){
		//logic
	}
}
```

All the rules will be verified from left to right, until one of then fails or all of then succeeds. 
The `RuleHandler` used will be the one defined at the `@HandledBy` of the rule that returned false 
unless you defined other handler at the action.

###Using rules in view

If you are running in a servlet 3 container, you can verify if a rule is satisfied in the view, using the object `rules`. For example:

```
<c:if test="${rules[CanAccessCar].isAllowed(car)}">
	<a href="brutauth/showCar">Show car</a>
</c:if>
```



