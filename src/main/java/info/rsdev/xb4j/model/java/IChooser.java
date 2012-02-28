package info.rsdev.xb4j.model.java;

import info.rsdev.xb4j.model.ChoiceBinding;

/**
 * Select the choice from {@link ChoiceBinding} that is applicable for the object tree that is currently being marshalled
 * @author Dave Schoorl
 */
public interface IChooser {

	public boolean matches(Object javaContext);
}
