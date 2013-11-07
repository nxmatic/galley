package org.commonjava.maven.galley.maven.model.view;

import java.util.ArrayList;
import java.util.List;

import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.GalleyMavenRuntimeException;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

public class MavenElementView
{

    //    private final Logger logger = new Logger( getClass() );

    protected final NodeRef element;

    protected final MavenPomView pomView;

    private final String managementXpathFragment;

    private String[] managementXpaths;

    public MavenElementView( final MavenPomView pomView, final NodeRef element, final String managementXpathFragment )
    {
        this.pomView = pomView;
        this.element = element;
        this.managementXpathFragment = managementXpathFragment;
    }

    public MavenElementView( final MavenPomView pomView, final NodeRef element )
    {
        this.pomView = pomView;
        this.element = element;
        this.managementXpathFragment = null;
    }

    /**
     * Override this to provide the xpath fragment used to find management values for this view.
     */
    protected String getManagedViewQualifierFragment()
    {
        return null;
    }

    protected boolean containsExpression( final String value )
    {
        return pomView.containsExpression( value );
    }

    public NodeRef getElement()
    {
        return element;
    }

    public MavenPomView getPomView()
    {
        return pomView;
    }

    public String getProfileId()
    {
        return pomView.getProfileIdFor( element );
    }

    protected String getValueWithManagement( final String named )
    {
        String value = getValue( named );
        //        logger.info( "Value of path: '%s' local to: %s is: '%s'\nIn: %s", named, element, value, pomView.getRef() );
        if ( value == null )
        {
            final String[] managementXpaths = managementXpathsFor( named + "/text()" );
            for ( final String managementXpath : managementXpaths )
            {
                if ( managementXpath != null )
                {
                    value = pomView.resolveXPathExpression( managementXpath, false, -1 );
                    //                    logger.info( "Value of management xpath: '%s' in %s is '%s'\nIn: %s", named, element, value, pomView.getRef() );
                    if ( value != null )
                    {
                        break;
                    }
                }
            }
        }

        return value;
    }

    protected List<NodeRef> getAggregateNodesWithManagement( final String path )
        throws GalleyMavenException
    {
        List<NodeRef> nodes = pomView.resolveXPathToNodeListFrom( this.element, path );
        if ( nodes == null || nodes.isEmpty() )
        {
            final String[] xpaths = managementXpathsFor( path );
            for ( final String xpath : xpaths )
            {
                nodes = pomView.resolveXPathToAggregatedNodeList( xpath, -1 );
                if ( nodes != null && !nodes.isEmpty() )
                {
                    break;
                }
            }
        }

        return nodes;
    }

    protected List<NodeRef> getFirstNodesWithManagement( final String path )
    {
        //        logger.info( "Resolving '%s' from node: %s", path, this.element );
        List<NodeRef> nodes = pomView.resolveXPathToNodeListFrom( this.element, path );
        if ( nodes == null || nodes.isEmpty() )
        {
            final String[] xpaths = managementXpathsFor( path );
            for ( final String xpath : xpaths )
            {
                //                logger.info( "Resolving '%s' from POM hierarchy.", xpath );
                nodes = pomView.resolveXPathToFirstNodeList( xpath, -1 );
                if ( nodes != null && !nodes.isEmpty() )
                {
                    break;
                }
            }
        }

        return nodes;
    }

    private String[] managementXpathsFor( final String named )
    {
        initManagementXpaths();
        if ( managementXpaths == null )
        {
            return null;
        }

        final String[] result = new String[managementXpaths.length];
        for ( int i = 0; i < result.length; i++ )
        {
            //            logger.info( "Customizing management XPath: '%s' for: '%s'", managementXpaths[i], named );
            result[i] = String.format( managementXpaths[i], named );
        }

        return result;
    }

    private void initManagementXpaths()
    {
        if ( managementXpathFragment == null )
        {
            return;
        }

        final String qualifier = getManagedViewQualifierFragment();
        if ( qualifier == null )
        {
            return;
        }

        final List<String> xpaths = new ArrayList<>();

        final String profileId = getProfileId();
        if ( profileId != null )
        {
            final StringBuilder sb = new StringBuilder();

            sb.append( "/project/profiles/profile[id/text()=\"" )
              .append( profileId )
              .append( "\"]/" )
              .append( managementXpathFragment )
              .append( '[' )
              .append( qualifier )
              .append( "]/%s" );

            final String xp = sb.toString();
            xpaths.add( xp );
            //            logger.info( "Created management XPath template: '%s'", xp );
        }

        final StringBuilder sb = new StringBuilder();
        sb.append( "/project/" )
          .append( managementXpathFragment )
          .append( '[' )
          .append( qualifier )
          .append( "]/%s" );

        final String xp = sb.toString();
        xpaths.add( xp );
        //        logger.info( "Created management XPath template: '%s'", xp );

        managementXpaths = xpaths.toArray( new String[xpaths.size()] );
    }

    protected String getValue( final String path )
    {
        return pomView.resolveExpressions( pomView.resolveXPathToNodeFrom( element, path ) );
    }

    protected NodeRef getNode( final String path )
    {
        final VTDNav nav = element.getNav()
                                  .cloneNav();
        final AutoPilot ap = new AutoPilot( nav );
        try
        {
            ap.selectXPath( path );
        }
        catch ( final XPathParseException e )
        {
            throw new GalleyMavenRuntimeException( "Failed to compile xpath expression: %s. Reason: %s", e, path, e.getMessage() );
        }

        int idx;
        try
        {
            idx = ap.evalXPath();
        }
        catch ( XPathEvalException | NavException e )
        {
            throw new GalleyMavenRuntimeException( "Failed to resolve content for xpath expression: %s. Reason: %s", e, path, e.getMessage() );
        }

        if ( idx > -1 )
        {
            return new NodeRef( nav, idx );
        }

        return null;
    }

    public String toXML()
    {
        return pomView.toXML( element );
    }

}