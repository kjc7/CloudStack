/*
 * Copyright (c) 2006-2010 Citrix Systems, Inc.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation, with the additional linking exception as
 * follows:
 * 
 *   Linking this library statically or dynamically with other modules is
 *   making a combined work based on this library. Thus, the terms and
 *   conditions of the GNU General Public License cover the whole combination.
 * 
 *   As a special exception, the copyright holders of this library give you
 *   permission to link this library with independent modules to produce an
 *   executable, regardless of the license terms of these independent modules,
 *   and to copy and distribute the resulting executable under terms of your
 *   choice, provided that you also meet, for each linked independent module,
 *   the terms and conditions of the license of that module. An independent
 *   module is a module which is not derived from or based on this library. If
 *   you modify this library, you may extend this exception to your version of
 *   the library, but you are not obligated to do so. If you do not wish to do
 *   so, delete this exception statement from your version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.xensource.xenapi;

import com.xensource.xenapi.Types.BadServerResponse;
import com.xensource.xenapi.Types.VersionException;
import com.xensource.xenapi.Types.XenAPIException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.xmlrpc.XmlRpcException;

/**
 * A tunnel for network traffic
 *
 * @author Citrix Systems, Inc.
 */
public class Tunnel extends XenAPIObject {

    /**
     * The XenAPI reference to this object.
     */
    protected final String ref;

    /**
     * For internal use only.
     */
    Tunnel(String ref) {
       this.ref = ref;
    }

    public String toWireString() {
       return this.ref;
    }

    /**
     * If obj is a Tunnel, compares XenAPI references for equality.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj != null && obj instanceof Tunnel)
        {
            Tunnel other = (Tunnel) obj;
            return other.ref.equals(this.ref);
        } else
        {
            return false;
        }
    }

    @Override
    public int hashCode()
    {
        return ref.hashCode();
    }

    /**
     * Represents all the fields in a Tunnel
     */
    public static class Record implements Types.Record {
        public String toString() {
            StringWriter writer = new StringWriter();
            PrintWriter print = new PrintWriter(writer);
            print.printf("%1$20s: %2$s\n", "uuid", this.uuid);
            print.printf("%1$20s: %2$s\n", "accessPIF", this.accessPIF);
            print.printf("%1$20s: %2$s\n", "transportPIF", this.transportPIF);
            print.printf("%1$20s: %2$s\n", "status", this.status);
            print.printf("%1$20s: %2$s\n", "otherConfig", this.otherConfig);
            return writer.toString();
        }

        /**
         * Convert a tunnel.Record to a Map
         */
        public Map<String,Object> toMap() {
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("uuid", this.uuid == null ? "" : this.uuid);
            map.put("access_PIF", this.accessPIF == null ? new PIF("OpaqueRef:NULL") : this.accessPIF);
            map.put("transport_PIF", this.transportPIF == null ? new PIF("OpaqueRef:NULL") : this.transportPIF);
            map.put("status", this.status == null ? new HashMap<String, String>() : this.status);
            map.put("other_config", this.otherConfig == null ? new HashMap<String, String>() : this.otherConfig);
            return map;
        }

        /**
         * Unique identifier/object reference
         */
        public String uuid;
        /**
         * The interface through which the tunnel is accessed
         */
        public PIF accessPIF;
        /**
         * The interface used by the tunnel
         */
        public PIF transportPIF;
        /**
         * Status information about the tunnel
         */
        public Map<String, String> status;
        /**
         * Additional configuration
         */
        public Map<String, String> otherConfig;
    }

    /**
     * Get a record containing the current state of the given tunnel.
     *
     * @return all fields from the object
     */
    public Tunnel.Record getRecord(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "tunnel.get_record";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toTunnelRecord(result);
    }

    /**
     * Get a reference to the tunnel instance with the specified UUID.
     *
     * @param uuid UUID of object to return
     * @return reference to the object
     */
    public static Tunnel getByUuid(Connection c, String uuid) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "tunnel.get_by_uuid";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(uuid)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toTunnel(result);
    }

    /**
     * Get the uuid field of the given tunnel.
     *
     * @return value of the field
     */
    public String getUuid(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "tunnel.get_uuid";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toString(result);
    }

    /**
     * Get the access_PIF field of the given tunnel.
     *
     * @return value of the field
     */
    public PIF getAccessPIF(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "tunnel.get_access_PIF";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toPIF(result);
    }

    /**
     * Get the transport_PIF field of the given tunnel.
     *
     * @return value of the field
     */
    public PIF getTransportPIF(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "tunnel.get_transport_PIF";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toPIF(result);
    }

    /**
     * Get the status field of the given tunnel.
     *
     * @return value of the field
     */
    public Map<String, String> getStatus(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "tunnel.get_status";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toMapOfStringString(result);
    }

    /**
     * Get the other_config field of the given tunnel.
     *
     * @return value of the field
     */
    public Map<String, String> getOtherConfig(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "tunnel.get_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toMapOfStringString(result);
    }

    /**
     * Set the status field of the given tunnel.
     *
     * @param status New value to set
     */
    public void setStatus(Connection c, Map<String, String> status) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "tunnel.set_status";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(status)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Add the given key-value pair to the status field of the given tunnel.
     *
     * @param key Key to add
     * @param value Value to add
     */
    public void addToStatus(Connection c, String key, String value) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "tunnel.add_to_status";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(key), Marshalling.toXMLRPC(value)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Remove the given key and its corresponding value from the status field of the given tunnel.  If the key is not in that Map, then do nothing.
     *
     * @param key Key to remove
     */
    public void removeFromStatus(Connection c, String key) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "tunnel.remove_from_status";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(key)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Set the other_config field of the given tunnel.
     *
     * @param otherConfig New value to set
     */
    public void setOtherConfig(Connection c, Map<String, String> otherConfig) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "tunnel.set_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(otherConfig)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Add the given key-value pair to the other_config field of the given tunnel.
     *
     * @param key Key to add
     * @param value Value to add
     */
    public void addToOtherConfig(Connection c, String key, String value) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "tunnel.add_to_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(key), Marshalling.toXMLRPC(value)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Remove the given key and its corresponding value from the other_config field of the given tunnel.  If the key is not in that Map, then do nothing.
     *
     * @param key Key to remove
     */
    public void removeFromOtherConfig(Connection c, String key) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "tunnel.remove_from_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(key)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Create a tunnel
     *
     * @param transportPIF PIF which receives the tagged traffic
     * @param network Network to receive the tunnelled traffic
     * @return Task
     */
    public static Task createAsync(Connection c, PIF transportPIF, Network network) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException,
       Types.OpenvswitchNotActive,
       Types.TransportPifNotConfigured,
       Types.IsTunnelAccessPif {
        String method_call = "Async.tunnel.create";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(transportPIF), Marshalling.toXMLRPC(network)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
        return Types.toTask(result);
    }

    /**
     * Create a tunnel
     *
     * @param transportPIF PIF which receives the tagged traffic
     * @param network Network to receive the tunnelled traffic
     * @return The reference of the created tunnel object
     */
    public static Tunnel create(Connection c, PIF transportPIF, Network network) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException,
       Types.OpenvswitchNotActive,
       Types.TransportPifNotConfigured,
       Types.IsTunnelAccessPif {
        String method_call = "tunnel.create";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(transportPIF), Marshalling.toXMLRPC(network)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toTunnel(result);
    }

    /**
     * Destroy a tunnel
     *
     * @return Task
     */
    public Task destroyAsync(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "Async.tunnel.destroy";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
        return Types.toTask(result);
    }

    /**
     * Destroy a tunnel
     *
     */
    public void destroy(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "tunnel.destroy";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Return a list of all the tunnels known to the system.
     *
     * @return references to all objects
     */
    public static Set<Tunnel> getAll(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "tunnel.get_all";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toSetOfTunnel(result);
    }

    /**
     * Return a map of tunnel references to tunnel records for all tunnels known to the system.
     *
     * @return records of all objects
     */
    public static Map<Tunnel, Tunnel.Record> getAllRecords(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "tunnel.get_all_records";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toMapOfTunnelTunnelRecord(result);
    }

}