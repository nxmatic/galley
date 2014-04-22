/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.maven.galley.maven.model.view;

import static org.commonjava.maven.galley.maven.model.view.XPathManager.A;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.AND;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.END_PAREN;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.EQQUOTE;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.G;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.QUOTE;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.RESOLVE;
import static org.commonjava.maven.galley.maven.model.view.XPathManager.TEXT;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.commonjava.maven.galley.maven.GalleyMavenException;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginDefaults;
import org.commonjava.maven.galley.maven.spi.defaults.MavenPluginImplications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class PluginView
    extends MavenGAVView
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final MavenPluginDefaults pluginDefaults;

    private List<PluginDependencyView> pluginDependencies;

    private final MavenPluginImplications pluginImplications;

    protected PluginView( final MavenPomView pomView, final Element element, final MavenPluginDefaults pluginDefaults,
                          final MavenPluginImplications pluginImplications )
    {
        super( pomView, element, "build/pluginManagement/plugins/plugin" );
        this.pluginDefaults = pluginDefaults;
        this.pluginImplications = pluginImplications;
    }

    public boolean isManaged()
        throws GalleyMavenException
    {
        return pomView.resolveXPathToNodeFrom( elementContext, "ancestor::pluginManagement", true ) != null;
    }

    public synchronized List<PluginDependencyView> getLocalPluginDependencies()
        throws GalleyMavenException
    {
        if ( pluginDependencies == null )
        {
            final List<PluginDependencyView> result = new ArrayList<PluginDependencyView>();

            final List<Node> nodes = getFirstNodesWithManagement( "dependencies/dependency" );
            if ( nodes != null )
            {
                for ( final Node node : nodes )
                {
                    logger.debug( "Adding plugin dependency for: {}", node.getNodeName() );
                    result.add( new PluginDependencyView( pomView, this, (Element) node ) );
                }

                this.pluginDependencies = result;
            }
        }

        return pluginDependencies;
    }

    public Set<PluginDependencyView> getImpliedPluginDependencies()
        throws GalleyMavenException
    {
        return pluginImplications.getImpliedPluginDependencies( this );
    }

    @Override
    public synchronized String getVersion()
        throws GalleyMavenException
    {
        if ( super.getVersion() == null )
        {
            setVersion( pluginDefaults.getDefaultVersion( getGroupId(), getArtifactId() ) );
        }

        return super.getVersion();
    }

    @Override
    public synchronized String getGroupId()
    {
        final String gid = super.getGroupId();
        if ( gid == null )
        {
            setGroupId( pluginDefaults.getDefaultGroupId( getArtifactId() ) );
        }

        return super.getGroupId();
    }

    @Override
    protected String getManagedViewQualifierFragment()
    {
        final StringBuilder sb = new StringBuilder();

        final String aid = getArtifactId();
        final String gid = getGroupId();
        final String dgid = pluginDefaults.getDefaultGroupId( aid );
        if ( !gid.equals( dgid ) )
        {
            sb.append( RESOLVE )
              .append( G )
              .append( TEXT )
              .append( END_PAREN )
              .append( EQQUOTE )
              .append( gid )
              .append( QUOTE )
              .append( AND );
        }

        sb.append( RESOLVE )
          .append( A )
          .append( TEXT )
          .append( END_PAREN )
          .append( EQQUOTE )
          .append( aid )
          .append( QUOTE );

        return sb.toString();
    }

}
