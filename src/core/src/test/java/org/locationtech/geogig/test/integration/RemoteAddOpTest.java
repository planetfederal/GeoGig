/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.test.integration;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.locationtech.geogig.api.Remote;
import org.locationtech.geogig.api.porcelain.RemoteAddOp;
import org.locationtech.geogig.api.porcelain.RemoteException;

public class RemoteAddOpTest extends RepositoryTestCase {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public final void setUpInternal() {
    }

    @Test
    public void testNullName() {
        final RemoteAddOp remoteAdd = geogit.command(RemoteAddOp.class);

        exception.expect(RemoteException.class);
        remoteAdd.setName(null).setURL("http://test.com").call();
    }

    @Test
    public void testEmptyName() {
        final RemoteAddOp remoteAdd = geogit.command(RemoteAddOp.class);

        exception.expect(RemoteException.class);
        remoteAdd.setName("").setURL("http://test.com").call();
    }

    @Test
    public void testNullURL() {
        final RemoteAddOp remoteAdd = geogit.command(RemoteAddOp.class);

        exception.expect(RemoteException.class);
        remoteAdd.setName("myremote").setURL(null).call();
    }

    @Test
    public void testEmptyURL() {
        final RemoteAddOp remoteAdd = geogit.command(RemoteAddOp.class);

        exception.expect(RemoteException.class);
        remoteAdd.setName("myremote").setURL("").call();
    }

    @Test
    public void testAddRemoteNullBranch() {
        final RemoteAddOp remoteAdd = geogit.command(RemoteAddOp.class);

        String remoteName = "myremote";
        String remoteURL = "http://test.com";

        Remote remote = remoteAdd.setName(remoteName).setURL(remoteURL).setBranch(null).call();

        assertEquals(remoteName, remote.getName());
        assertEquals(remoteURL, remote.getFetchURL());
        assertEquals(remoteURL, remote.getPushURL());
        assertEquals("+refs/heads/*:refs/remotes/" + remoteName + "/*", remote.getFetch());
    }

    @Test
    public void testAddRemoteEmptyBranch() {
        final RemoteAddOp remoteAdd = geogit.command(RemoteAddOp.class);

        String remoteName = "myremote";
        String remoteURL = "http://test.com";

        Remote remote = remoteAdd.setName(remoteName).setURL(remoteURL).setBranch("").call();

        assertEquals(remoteName, remote.getName());
        assertEquals(remoteURL, remote.getFetchURL());
        assertEquals(remoteURL, remote.getPushURL());
        assertEquals("+refs/heads/*:refs/remotes/" + remoteName + "/*", remote.getFetch());
    }

    @Test
    public void testAddRemoteWithBranch() {
        final RemoteAddOp remoteAdd = geogit.command(RemoteAddOp.class);

        String remoteName = "myremote";
        String remoteURL = "http://test.com";
        String branch = "mybranch";

        Remote remote = remoteAdd.setName(remoteName).setURL(remoteURL).setBranch(branch).call();

        assertEquals(remoteName, remote.getName());
        assertEquals(remoteURL, remote.getFetchURL());
        assertEquals(remoteURL, remote.getPushURL());
        assertEquals("+refs/heads/" + branch + ":refs/remotes/" + remoteName + "/" + branch,
                remote.getFetch());
    }

    @Test
    public void testAddRemoteThatExists() {
        final RemoteAddOp remoteAdd = geogit.command(RemoteAddOp.class);

        String remoteName = "myremote";
        String remoteURL = "http://test.com";

        Remote remote = remoteAdd.setName(remoteName).setURL(remoteURL).call();

        assertEquals(remoteName, remote.getName());
        assertEquals(remoteURL, remote.getFetchURL());
        assertEquals(remoteURL, remote.getPushURL());
        assertEquals("+refs/heads/*:refs/remotes/" + remoteName + "/*", remote.getFetch());

        exception.expect(RemoteException.class);
        remoteAdd.setName(remoteName).setURL("someotherurl.com").call();
    }

    @Test
    public void testAddMultipleRemotes() {
        final RemoteAddOp remoteAdd = geogit.command(RemoteAddOp.class);

        String remoteName1 = "myremote";
        String remoteURL1 = "http://test.com";

        String remoteName2 = "myremote2";
        String remoteURL2 = "http://test2.org";

        Remote remote = remoteAdd.setName(remoteName1).setURL(remoteURL1).call();

        assertEquals(remoteName1, remote.getName());
        assertEquals(remoteURL1, remote.getFetchURL());
        assertEquals(remoteURL1, remote.getPushURL());
        assertEquals("+refs/heads/*:refs/remotes/" + remoteName1 + "/*", remote.getFetch());

        remote = remoteAdd.setName(remoteName2).setURL(remoteURL2).call();

        assertEquals(remoteName2, remote.getName());
        assertEquals(remoteURL2, remote.getFetchURL());
        assertEquals(remoteURL2, remote.getPushURL());
        assertEquals("+refs/heads/*:refs/remotes/" + remoteName2 + "/*", remote.getFetch());
    }

}
