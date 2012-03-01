package info.rsdev.xb4j.model.java;

import info.rsdev.xb4j.model.ChoiceBinding;

/**
 * An {@link IChooser} is used during the marshalling process (from java to xml). It select's the choice from {@link ChoiceBinding} 
 * that is applicable for the object currently being marshalled.
 * 
 * @author Dave Schoorl
 */
public interface IChooser {

	public boolean matches(Object javaContext);
}
