package org.dockfx;

import java.util.LinkedList;
import java.util.Properties;

/**
 * ContentHolder has common functions for storing persistent object for node
 *
 * @author HongKee Moon
 */
public class ContentHolder
{
	/**
	 * The enum ContentHolder Type.
	 */
	public enum Type {
		/**
		 * The SplitPane.
		 */
		SplitPane,
		/**
		 * The TabPane.
		 */
		TabPane,
		/**
		 * The Collection.
		 */
		Collection,
		/**
		 * The FloatingNode.
		 */
		FloatingNode,
		/**
		 * The DockNode.
		 */
		DockNode
	}

	String name;
	Properties properties;
	LinkedList children;
	Type type;

	public ContentHolder()
	{

	}

	public ContentHolder( String name, Type type )
	{
		this.name = name;
		this.properties = new Properties();
		this.children = new LinkedList();
		this.type = type;
	}

	public void addProperty( Object key, Object value )
	{
		properties.put( key, value );
	}

	public void addChild( Object child )
	{
		children.add( child );
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public Properties getProperties()
	{
		return properties;
	}

	public void setProperties( Properties properties )
	{
		this.properties = properties;
	}

	public LinkedList getChildren()
	{
		return children;
	}

	public void setChildren( LinkedList children )
	{
		this.children = children;
	}

	public Type getType()
	{
		return type;
	}

	public void setType( Type type )
	{
		this.type = type;
	}
}
