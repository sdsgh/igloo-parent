package org.iglooproject.config.bootstrap.spring.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * <p>This annotation setups three priority level for property-sources : component, framework and application.</p>
 * 
 * <p>Those levels go from lower to higher precedence.</p>
 * 
 * <p>This annotation <b>must be setup before any other {@link PropertySource} using those priority level</b>.</p>
 * 
 * <p>It is advised :
 * <ul>
 * <li>To annotate your first imported @{@link Configuration} class</li>
 * <li>Not do declare any other @{@link PropertySource} on this same class</li>
 * <li>It is not a problem if this annotation is declared multiple times in the same context, so it is safe to
 * annotate both &lt;application&gt;CoreConfig and &lt;application&gt;WebappConfig; only the first encountered
 * annotation is important</li>
 * </ul>
 * </p>
 * 
 * <p>
 * If you do not need to setup @{@link PropertySource} on your main configuration class:
 * <pre class="code">
 * &#064;Configuration
 * &#064;IglooPropertySourcesLevels
 * public class MainClass {
 * ...
 * }
 * </pre>
 * </p>
 * 
 * <p>
 * If you need to setup a {@link PropertySource} on your main configuration class:
 * <pre class="code">
 * &#064;Configuration
 * &#064;PropertySource(... your definition ...)
 * public class MainClass {
 *   &sol;* Setup Igloo PropertySource levels *&sol;
 *   &#064;Configuration
 *   &#064;IglooPropertySourcesLevels
 *   public static class IglooPropertySourcesLevelsConfig {  &sol;* empty *&sol; };
 * }
 * </pre>
 * </p>
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PropertySource(name = IglooPropertySourcePriority.COMPONENT, value = "classpath:igloo-placeholder.properties")
@PropertySource(name = IglooPropertySourcePriority.FRAMEWORK, value = "classpath:igloo-placeholder.properties")
@PropertySource(name = IglooPropertySourcePriority.APPLICATION, value = "classpath:igloo-placeholder.properties")
@PropertySource(name = IglooPropertySourcePriority.BOOTSTRAP, value = "classpath:igloo-placeholder.properties")
@PropertySource(name = IglooPropertySourcePriority.OVERRIDES, value = "classpath:igloo-placeholder.properties")
public @interface IglooPropertySourcesLevels {

}
