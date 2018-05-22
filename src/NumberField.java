import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A class to manage the entry of a number by the user. We want to make sure that the value of this field can always be interpreted as a number.
 * Whenever RETURN is pressed, or focus shifts to another user interface object, the value is parsed. If it has changed, a client can be informed
 * of this through a ChangeEvent sent to a ChangeListener, in the same way as for a slider.
 * 
 * We make use of Java's NumberFormat class to decide what looks like a number. The DecimalFormat subclass is very helpful here.
 * 
 * @author Giovanni Brandani
 *
 */
public class NumberField extends JTextField {

	//  PRIVATE FIELDS
	
	private Number value;
	private NumberFormat format;
	private Set<ChangeListener> listeners = new HashSet<ChangeListener>();
	
	//  CONSTRUCTORS
	
	/**
	 * Create a JTextField that manages the input of a numerical value with specified initial value and format.
	 * 
	 * @param initValue initial value to put into the field
	 * @param useFormat format of numerical input
	 */
	public NumberField(Number initValue, NumberFormat useFormat)
	{
		super(useFormat.format(initValue));
		
		value = initValue;
		format = useFormat;
		
		// Event handlers to ensure the number entered is always valid and that changes get notified to listeners
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) checkAndUpdate();
			}
		});
		addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				checkAndUpdate();
			}
		});
	}
	

	/**
	 * Default constructor, set an initial value of zero and use the default number formatter
	 */
	public NumberField() {
		this(0.0, NumberFormat.getNumberInstance());
	}
	
	//  METHODS FOR TRACKING CHANGES
	
	/*
	 * Private method to fire off a change event to interested ChangeListeners
	 */
	private void fireChangeEvent() {
		ChangeEvent e = new ChangeEvent(this);
		for(ChangeListener l : listeners) {
			l.stateChanged(e);
		}		
	}
	
	/*
	 * Private method whose purpose is to check the entered number for validity, and to inform any interested ChangeListeners when a change occurs 
	 */
	private void checkAndUpdate() {
		// Check the number is valid; if so, update it and fire event
		Number newValue = value;
		try {
			newValue = format.parse(getText());		
			if(!value.equals(newValue)) {
				value = newValue;
				fireChangeEvent();
			}
		}
		catch(Exception e) { }
		setText(format.format(value));
	}

	/**
	 * Attach a ChangeListener object to the field. It is sent the stateChanged(ChangeEvent e) message when the value displayed in the field is changed.
	 * 
	 * @param l Interested ChangeListener object
	 */
	public void addChangeListener(ChangeListener l) {
		listeners.add(l);
	}
	
	/**
	 * Remove a ChangeListener object previously attached to the field.
	 * 
	 * @param l Interested ChangeListener object
	 */
	public void removeChangeListener(ChangeListener l) {
		listeners.remove(l);
	}
		
	//  ACCESSORS
	
	/**
	 * Set the value of the number in the field. For consistency with other Swing objects, this fires a change event if it causes the actual value
	 * to change.
	 * 
	 * @param newValue new value to display
	 */
	void setValue(Number newValue) {
		setText(format.format(newValue));
		if(!value.equals(newValue)) {
			value = newValue;
			fireChangeEvent();
		}
	}
	
	/**
	 * Get the most recently-entered valid number in the field
	 * 
	 * @return value displayed in the field
	 */
	String getValue() {
		return format.format(value);
	}

}
