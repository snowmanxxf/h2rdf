/*******************************************************************************
 * Copyright (c) 2012 Nikos Papailiou. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Nikos Papailiou - initial API and implementation
 ******************************************************************************/
package concurrent;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

/**
 * Barrier
 */
public class Barrier extends SyncPrimitive {
    int size;
    String name;

    /**
     * Barrier constructor
     *
     * @param address
     * @param root
     * @param size
     */
    Barrier(String address, String root, int size) {
        super(address);
        this.input = input;
        this.size = size;

        // Create barrier node
        if (zk != null) {
            try {
                Stat s = zk.exists(root, false);
                if (s == null) {
                    zk.create(input, new byte[0], Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);
                }
            } catch (KeeperException e) {
                System.out
                        .println("Keeper exception when instantiating queue: "
                                + e.toString());
            } catch (InterruptedException e) {
                System.out.println("Interrupted exception");
            }
        }

        // My node name
        try {
            name = new String(InetAddress.getLocalHost().getCanonicalHostName().toString());
        } catch (UnknownHostException e) {
            System.out.println(e.toString());
        }

    }

    /**
     * Join barrier
     *
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */

    boolean enter() throws KeeperException, InterruptedException{
        zk.create(input + "/" + name, new byte[0], Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
        while (true) {
            synchronized (mutex) {
                List<String> list = zk.getChildren(input, true);

                if (list.size() < size) {
                    mutex.wait();
                } else {
                    return true;
                }
            }
        }
    }

    /**
     * Wait until all reach barrier
     *
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */

    boolean leave() throws KeeperException, InterruptedException{
        zk.delete(input + "/" + name, 0);
        while (true) {
            synchronized (mutex) {
                List<String> list = zk.getChildren(input, true);
                    if (list.size() > 0) {
                        mutex.wait();
                    } else {
                        return true;
                    }
                }
            }
    }
}
