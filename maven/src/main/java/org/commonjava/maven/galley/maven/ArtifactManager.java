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
package org.commonjava.maven.galley.maven;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.commonjava.maven.atlas.ident.ref.ArtifactRef;
import org.commonjava.maven.atlas.ident.ref.ProjectVersionRef;
import org.commonjava.maven.atlas.ident.ref.TypeAndClassifier;
import org.commonjava.maven.galley.TransferException;
import org.commonjava.maven.galley.model.ArtifactBatch;
import org.commonjava.maven.galley.model.ConcreteResource;
import org.commonjava.maven.galley.model.Location;
import org.commonjava.maven.galley.model.Transfer;

public interface ArtifactManager
{

    boolean delete( Location location, ArtifactRef ref )
        throws TransferException;

    boolean deleteAll( List<? extends Location> locations, ArtifactRef ref )
        throws TransferException;

    ArtifactBatch batchRetrieve( ArtifactBatch batch )
        throws TransferException;

    ArtifactBatch batchRetrieveAll( ArtifactBatch batch )
        throws TransferException;

    Transfer retrieve( Location location, ArtifactRef ref )
        throws TransferException;

    List<Transfer> retrieveAll( List<? extends Location> locations, ArtifactRef ref )
        throws TransferException;

    Transfer retrieveFirst( List<? extends Location> locations, ArtifactRef ref )
        throws TransferException;

    Transfer store( Location location, ArtifactRef ref, InputStream stream )
        throws TransferException;

    boolean publish( Location location, ArtifactRef ref, InputStream stream, long length )
        throws TransferException;

    Map<TypeAndClassifier, ConcreteResource> listAvailableArtifacts( Location location, ProjectVersionRef ref )
        throws TransferException;

    ProjectVersionRef resolveVariableVersion( Location location, ProjectVersionRef ref )
        throws TransferException;

    ProjectVersionRef resolveVariableVersion( List<? extends Location> locations, ProjectVersionRef ref )
        throws TransferException;

    List<ConcreteResource> findAllExisting( List<? extends Location> locations, ArtifactRef ref )
        throws TransferException;

    ConcreteResource checkExistence( Location location, ArtifactRef ref )
        throws TransferException;

}
