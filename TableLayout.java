//
//  TableLayout.java
//
//  Created by Stefan Schoenberger on Fri Sep 06 2002.
//

package de.esefes.swing.utils;

import java.awt.*;
import java.util.*;

public class TableLayout implements LayoutManager, LayoutManager2 {
	private TableContainer table = null;
	private int rowPadding = 0;
	private int colPadding = 0;

	public TableLayout( String definition ) {
		this( definition, 0, 0 );
	}

	public TableLayout( String definition, int padding ) {
		this( definition, padding, padding );
	}

	public TableLayout( String definition, int rowPadding, int colPadding ) {
		this.rowPadding = rowPadding;
		this.colPadding = colPadding;
		if ( definition == null )
			throw new RuntimeException( "TableLayout: definition missing" );
		else
			parseDefinition( definition );
	}

	protected void parseDefinition( String definition ) {
		table = new TableContainer();
		for ( int i = 0; i < definition.length(); i++ ) {
			TableLayoutConstraint con;
			switch ( definition.charAt( i ) ) {
			case 'l':
				con = new TableLayoutConstraint( TableLayoutConstraint.LEFT );
				break;
			case 'r':
				con = new TableLayoutConstraint( TableLayoutConstraint.RIGHT );
				break;
			case 'c':
				con = new TableLayoutConstraint( TableLayoutConstraint.CENTER );
				break;
			case 'f':
				con = new TableLayoutConstraint( 1.0 );
				break;
			default:
				con = new TableLayoutConstraint();
				break;
			}
			table.addColumn( new TableLayoutColumn( con ) );
		}
	}

	@Override
	public void addLayoutComponent( String name, Component comp ) {
		addLayoutComponent( comp, null );
	}

	@Override
	public void addLayoutComponent( Component comp, Object constraints ) {
		table.add( comp );
	}

	@Override
	public void removeLayoutComponent( Component comp ) {
		table.remove( comp );
	}

	@Override
	public Dimension maximumLayoutSize( Container target ) {
		return target.getMaximumSize();
	}

	@Override
	public float getLayoutAlignmentX( Container target ) {
		return 0.0f;
	}

	@Override
	public float getLayoutAlignmentY( Container target ) {
		return 0.0f;
	}

	@Override
	public void invalidateLayout( Container target ) {
	}

	private interface CompSizeVisitor {
		public Dimension getSize( Component comp, TableLayoutColumn col );
	}

	private Dimension calculateSize( CompSizeVisitor size ) {
		int rows = table.getRowCount();
		int cols = table.getColumnCount();
		int totalWidth = 0;
		int totalHeight = 0;

		for ( int r = 0; r < rows; r++ ) {
			TableLayoutRow tlr = table.getRow( r );
			tlr.setHeight( 0 );
		}
		for ( int c = 0; c < cols; c++ ) {
			TableLayoutColumn tlc = table.getColumn( c );
			tlc.setWidth( 0 );
			for ( int r = 0; r < rows; r++ ) {
				TableLayoutRow tlr = table.getRow( r );
				Component comp = table.get( r, c );
				if ( comp != null && comp.isVisible() ) {
					Dimension d = size.getSize( comp, tlc );
					tlc.setWidth( Math.max( tlc.getWidth(), d.width ) );
					tlr.setHeight( Math.max( tlr.getHeight(), d.height ) );
				}
			}
			totalWidth += tlc.getWidth();
		}
		for ( int r = 0; r < rows; r++ ) {
			TableLayoutRow tlr = table.getRow( r );
			totalHeight += tlr.getHeight();
		}
		if ( rows > 1 )
			totalHeight += (rows - 1) * rowPadding;
		if ( cols > 1 )
			totalWidth += (cols - 1) * colPadding;
		return new Dimension( totalWidth, totalHeight );
	}

	private Dimension calculatePreferredSize() {
		return calculateSize( new CompSizeVisitor() {
			@Override
			public Dimension getSize( Component comp, TableLayoutColumn col ) {
				return comp.getPreferredSize();
			}
		} );
	}

	private Dimension calculateMinimumSize() {
		return calculateSize( new CompSizeVisitor() {
			@Override
			public Dimension getSize( Component comp, TableLayoutColumn col ) {
				return comp.getMinimumSize();
			}
		} );
	}

	@Override
	public Dimension preferredLayoutSize( Container parent ) {
		Dimension dim = calculatePreferredSize();

		Insets insets = parent.getInsets();
		dim.width += insets.left + insets.right;
		dim.height += insets.top + insets.bottom;

		return dim;
	}

	@Override
	public Dimension minimumLayoutSize( Container parent ) {
		Dimension dim = calculateMinimumSize();
		Insets insets = parent.getInsets();
		dim.width += insets.left + insets.right;
		dim.height += insets.top + insets.bottom;
		return dim;
	}

	@Override
	public void layoutContainer( Container parent ) {
		Insets insets = parent.getInsets();
		int maxWidth = parent.getSize().width - (insets.left + insets.right);
		int maxHeight = parent.getSize().height - (insets.top + insets.bottom);
		double weightSum = 0.0;
		int fixedWidth = 0;
		int preferredHeight = 0;
		int rows = table.getRowCount();
		int cols = table.getColumnCount();

		calculatePreferredSize();
		for ( int c = 0; c < cols; c++ ) {
			TableLayoutColumn tlc = table.getColumn( c );
			if ( tlc.isFlexible() )
				weightSum += tlc.getWeight();
			else
				fixedWidth += tlc.getWidth();
		}
		if ( cols > 1 )
			fixedWidth += (cols - 1) * colPadding;
		if ( maxWidth > fixedWidth ) {
			int totalWidth = 0;
			int flexCount = 0;
			double space = (maxWidth - fixedWidth) / weightSum;
			for ( int c = 0; c < cols; c++ ) {
				TableLayoutColumn tlc = table.getColumn( c );
				if ( tlc.isFlexible() ) {
					tlc.setWidth( (int)(space * tlc.getWeight()) );
					flexCount += 1;
				}
				totalWidth += tlc.getWidth();
			}
			if ( cols > 1 )
				totalWidth += (cols - 1) * colPadding;
			int roundedOff = maxWidth - totalWidth;
			if ( roundedOff > 0 && flexCount > 0 ) {
				for ( int c = cols - 1; c >= 0; c-- ) {
					TableLayoutColumn tlc = table.getColumn( c );
					if ( tlc.isFlexible() ) {
						tlc.setWidth( tlc.getWidth() + roundedOff );
						break;
					}
				}
			}
		} else {
			for ( int c = 0; c < cols; c++ ) {
				TableLayoutColumn tlc = table.getColumn( c );
				if ( tlc.isFlexible() )
					tlc.setWidth( 0 );
			}
		}
		int x0 = insets.left;
		int y = insets.top;
		for ( int r = 0; r < rows; r++ ) {
			int x = x0;
			TableLayoutRow tlr = table.getRow( r );
			for ( int c = 0; c < cols; c++ ) {
				TableLayoutColumn tlc = table.getColumn( c );
				Component comp = table.get( r, c );
				tlc.setBounds( comp, x, y, tlc.getWidth(), tlr.getHeight() );
				x += tlc.getWidth() + colPadding;
			}
			y += tlr.getHeight() + rowPadding;
		}
	}

	@Override
	public String toString() {
		return getClass().getName();
	}
}

class TableLayoutConstraint {
	public boolean flexible;
	public double weight;
	public int orientation;

	public final static int LEFT = 0;
	public final static int RIGHT = 1;
	public final static int CENTER = 2;

	public TableLayoutConstraint() {
		this( false, 1.0, LEFT );
	}

	public TableLayoutConstraint( double weight ) {
		this( true, weight, LEFT );
	}

	public TableLayoutConstraint( int orientation ) {
		this( false, 1.0, orientation );
	}

	private TableLayoutConstraint( boolean flexible, double weight, int orientation ) {
		this.flexible = flexible;
		this.weight = weight;
		this.orientation = orientation;
	}

	public boolean isFlexible() {
		return flexible;
	}

	public double getWeight() {
		return weight;
	}

	public void setBounds( Component comp, int x, int y, int w, int h ) {
		if ( flexible )
			comp.setBounds( x, y, w, h );
		else {
			Dimension d = comp.getPreferredSize();
			int width = Math.min( d.width, w );
			switch ( orientation ) {
			case RIGHT:
				x += w - width;
				break;
			case CENTER:
				x += (w - width) / 2;
				break;
			default:
				break;
			}
			comp.setBounds( x, y, width, h );
		}
	}
}

class TableLayoutColumn {
	private TableLayoutConstraint constraint;
	private int width;

	public TableLayoutColumn( TableLayoutConstraint constraint ) {
		this.constraint = constraint;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth( int width ) {
		this.width = width;
	}

	public boolean isFlexible() {
		return constraint.isFlexible();
	}

	public double getWeight() {
		return constraint.getWeight();
	}

	public void setBounds( Component comp, int x, int y, int w, int h ) {
		constraint.setBounds( comp, x, y, w, h );
	}
}

class TableLayoutRow {
	private int height;

	public int getHeight() {
		return height;
	}

	public void setHeight( int height ) {
		this.height = height;
	}
}

class TableContainer {
	private final static Component DUMMY_ELEMENT = new Label();
	private int rowCount = 0;
	private Vector items = new Vector();
	private Vector columns = new Vector();
	private Vector rows = new Vector();

	public void add( Component component ) {
		items.add( component );
		rowCount = calcRowCount();
		rows.setSize( rowCount );
	}

	public void remove( Component component ) {
		items.remove( component );
		rowCount = calcRowCount();
		rows.setSize( rowCount );
	}

	public void addColumn( TableLayoutColumn column ) {
		columns.add( column );
	}

	public TableLayoutColumn getColumn( int column ) {
		if ( column >= 0 && column < columns.size() )
			return (TableLayoutColumn)columns.get( column );
		else
			return null;
	}

	public TableLayoutRow getRow( int row ) {
		if ( row >= 0 && row < rowCount ) {
			TableLayoutRow r = (TableLayoutRow)rows.get( row );
			if ( r == null )
				rows.set( row, r = new TableLayoutRow() );
			return r;
		} else
			return null;
	}

	public int getItemCount() {
		return items.size();
	}

	public int getColumnCount() {
		return columns.size();
	}

	public int getRowCount() {
		return rowCount;
	}

	private int calcRowCount() {
		int items = getItemCount();
		int columns = getColumnCount();
		return (items + columns - 1) / columns;
	}

	public Component get( int i ) {
		if ( i >= 0 && i < items.size() )
			return (Component)items.get( i );
		else
			return DUMMY_ELEMENT;
	}

	public Component get( int row, int column ) {
		return get( row * getColumnCount() + column );
	}
}
