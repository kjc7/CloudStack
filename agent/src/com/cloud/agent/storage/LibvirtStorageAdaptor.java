/**
 *  Copyright (C) 2010 Cloud.com, Inc.  All rights reserved.
 *
 * This software is licensed under the GNU General Public License v3 or later.
 *
 * It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.cloud.agent.storage;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;
import org.libvirt.StoragePoolInfo;
import org.libvirt.StorageVol;
import org.libvirt.StoragePoolInfo.StoragePoolState;

import com.cloud.agent.api.ManageSnapshotCommand;
import com.cloud.agent.resource.computing.LibvirtConnection;
import com.cloud.agent.resource.computing.LibvirtStoragePoolDef;
import com.cloud.agent.resource.computing.LibvirtStoragePoolXMLParser;
import com.cloud.agent.resource.computing.LibvirtStorageVolumeDef;
import com.cloud.agent.resource.computing.LibvirtStoragePoolDef.poolType;
import com.cloud.agent.resource.computing.LibvirtStorageVolumeDef.volFormat;
import com.cloud.agent.resource.computing.LibvirtStorageVolumeXMLParser;
import com.cloud.agent.storage.KVMPhysicalDisk.PhysicalDiskFormat;
import com.cloud.exception.InternalErrorException;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StorageLayer;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.utils.script.OutputInterpreter;
import com.cloud.utils.script.Script;

public class LibvirtStorageAdaptor implements StorageAdaptor {
	private static final Logger s_logger = Logger
			.getLogger(LibvirtStorageAdaptor.class);
	private StorageLayer _storageLayer;
	private String _mountPoint = "/mnt";
	private String _manageSnapshotPath;

	public LibvirtStorageAdaptor(StorageLayer storage) {
		_storageLayer = storage;
		_manageSnapshotPath = Script.findScript("scripts/storage/qcow2/",
				"managesnapshot.sh");
	}

	@Override
	public boolean createFolder(String uuid, String path) {
		String mountPoint = _mountPoint + File.separator + uuid;
		File f = new File(mountPoint + path);
		if (!f.exists()) {
			f.mkdirs();
		}
		return true;
	}

	public StorageVol getVolume(StoragePool pool, String volName) {
		StorageVol vol = null;

		try {
			vol = pool.storageVolLookupByName(volName);
		} catch (LibvirtException e) {

		}
		if (vol == null) {
			storagePoolRefresh(pool);
			try {
				vol = pool.storageVolLookupByName(volName);
			} catch (LibvirtException e) {
				throw new CloudRuntimeException(e.toString());
			}
		}
		return vol;
	}

	public StorageVol createVolume(Connect conn, StoragePool pool, String uuid,
			long size, volFormat format) throws LibvirtException {
		LibvirtStorageVolumeDef volDef = new LibvirtStorageVolumeDef(UUID
				.randomUUID().toString(), size, format, null, null);
		s_logger.debug(volDef.toString());
		return pool.storageVolCreateXML(volDef.toString(), 0);
	}

	public StoragePool getStoragePoolbyURI(Connect conn, URI uri)
			throws LibvirtException {
		String sourcePath;
		String uuid;
		String sourceHost = "";
		String protocal;
		if (uri.getScheme().equalsIgnoreCase("local")) {
			sourcePath = _mountPoint + File.separator
					+ uri.toString().replace("local:///", "");
			sourcePath = sourcePath.replace("//", "/");
			uuid = UUID.nameUUIDFromBytes(new String(sourcePath).getBytes())
					.toString();
			protocal = "DIR";
		} else {
			sourcePath = uri.getPath();
			sourcePath = sourcePath.replace("//", "/");
			sourceHost = uri.getHost();
			uuid = UUID.nameUUIDFromBytes(
					new String(sourceHost + sourcePath).getBytes()).toString();
			protocal = "NFS";
		}

		String targetPath = _mountPoint + File.separator + uuid;
		StoragePool sp = null;
		try {
			sp = conn.storagePoolLookupByUUIDString(uuid);
		} catch (LibvirtException e) {
		}

		if (sp == null) {
			try {
				LibvirtStoragePoolDef spd = null;
				if (protocal.equalsIgnoreCase("NFS")) {
					_storageLayer.mkdir(targetPath);
					spd = new LibvirtStoragePoolDef(poolType.NETFS, uuid, uuid,
							sourceHost, sourcePath, targetPath);
					s_logger.debug(spd.toString());
					// addStoragePool(uuid);

				} else if (protocal.equalsIgnoreCase("DIR")) {
					_storageLayer.mkdir(targetPath);
					spd = new LibvirtStoragePoolDef(poolType.DIR, uuid, uuid,
							null, null, sourcePath);
				}

				synchronized (getStoragePool(uuid)) {
					sp = conn.storagePoolDefineXML(spd.toString(), 0);

					if (sp == null) {
						s_logger.debug("Failed to define storage pool");
						return null;
					}
					sp.create(0);
				}

				return sp;
			} catch (LibvirtException e) {
				try {
					if (sp != null) {
						sp.undefine();
						sp.free();
					}
				} catch (LibvirtException l) {

				}
				throw e;
			}
		} else {
			StoragePoolInfo spi = sp.getInfo();
			if (spi.state != StoragePoolState.VIR_STORAGE_POOL_RUNNING) {
				sp.create(0);
			}
			return sp;
		}
	}

	public void storagePoolRefresh(StoragePool pool) {
		try {
			synchronized (getStoragePool(pool.getUUIDString())) {
				pool.refresh(0);
			}
		} catch (LibvirtException e) {

		}
	}

	private StoragePool createNfsStoragePool(Connect conn, String uuid,
			String host, String path) {
		String targetPath = _mountPoint + File.separator + uuid;
		LibvirtStoragePoolDef spd = new LibvirtStoragePoolDef(poolType.NETFS,
				uuid, uuid, host, path, targetPath);
		_storageLayer.mkdir(targetPath);
		StoragePool sp = null;
		try {
			s_logger.debug(spd.toString());
			sp = conn.storagePoolDefineXML(spd.toString(), 0);
			sp.create(0);
			return sp;
		} catch (LibvirtException e) {
			s_logger.debug(e.toString());
			if (sp != null) {
				try {
					sp.undefine();
					sp.free();
				} catch (LibvirtException l) {
					s_logger.debug("Failed to define nfs storage pool with: "
							+ l.toString());
				}
			}
			return null;
		}
	}

	private StoragePool CreateSharedStoragePool(Connect conn, String uuid,
			String host, String path) {
		String mountPoint = path;
		if (!_storageLayer.exists(mountPoint)) {
			return null;
		}
		LibvirtStoragePoolDef spd = new LibvirtStoragePoolDef(poolType.DIR,
				uuid, uuid, host, path, path);
		StoragePool sp = null;
		try {
			s_logger.debug(spd.toString());
			sp = conn.storagePoolDefineXML(spd.toString(), 0);
			sp.create(0);

			return sp;
		} catch (LibvirtException e) {
			s_logger.debug(e.toString());
			if (sp != null) {
				try {
					sp.undefine();
					sp.free();
				} catch (LibvirtException l) {
					s_logger.debug("Failed to define shared mount point storage pool with: "
							+ l.toString());
				}
			}
			return null;
		}
	}

	private StoragePool createCLVMStoragePool(Connect conn, String uuid,
			String host, String path) {

		String volgroupPath = "/dev/" + path;
		String volgroupName = path;
		volgroupName = volgroupName.replaceFirst("/", "");

		LibvirtStoragePoolDef spd = new LibvirtStoragePoolDef(poolType.LOGICAL,
				volgroupName, uuid, host, volgroupPath, volgroupPath);
		StoragePool sp = null;
		try {
			s_logger.debug(spd.toString());
			sp = conn.storagePoolDefineXML(spd.toString(), 0);
			sp.create(0);
			return sp;
		} catch (LibvirtException e) {
			s_logger.debug(e.toString());
			if (sp != null) {
				try {
					sp.undefine();
					sp.free();
				} catch (LibvirtException l) {
					s_logger.debug("Failed to define clvm storage pool with: "
							+ l.toString());
				}
			}
			return null;
		}

	}

	public StorageVol copyVolume(StoragePool destPool,
			LibvirtStorageVolumeDef destVol, StorageVol srcVol, int timeout)
			throws LibvirtException {
		StorageVol vol = destPool.storageVolCreateXML(destVol.toString(), 0);
		String srcPath = srcVol.getKey();
		String destPath = vol.getKey();
		Script.runSimpleBashScript("cp " + srcPath + " " + destPath, timeout);
		return vol;
	}

	public boolean copyVolume(String srcPath, String destPath,
			String volumeName, int timeout) throws InternalErrorException {
		_storageLayer.mkdirs(destPath);
		if (!_storageLayer.exists(srcPath)) {
			throw new InternalErrorException("volume:" + srcPath
					+ " is not exits");
		}
		String result = Script.runSimpleBashScript("cp " + srcPath + " "
				+ destPath + File.separator + volumeName, timeout);
		if (result != null) {
			return false;
		} else {
			return true;
		}
	}

	public LibvirtStoragePoolDef getStoragePoolDef(Connect conn,
			StoragePool pool) throws LibvirtException {
		String poolDefXML = pool.getXMLDesc(0);
		LibvirtStoragePoolXMLParser parser = new LibvirtStoragePoolXMLParser();
		return parser.parseStoragePoolXML(poolDefXML);
	}

	public LibvirtStorageVolumeDef getStorageVolumeDef(Connect conn,
			StorageVol vol) throws LibvirtException {
		String volDefXML = vol.getXMLDesc(0);
		LibvirtStorageVolumeXMLParser parser = new LibvirtStorageVolumeXMLParser();
		return parser.parseStorageVolumeXML(volDefXML);
	}

	public StorageVol getVolumeFromURI(Connect conn, String volPath)
			throws LibvirtException, URISyntaxException {
		int index = volPath.lastIndexOf("/");
		URI volDir = null;
		StoragePool sp = null;
		StorageVol vol = null;
		try {
			volDir = new URI(volPath.substring(0, index));
			String volName = volPath.substring(index + 1);
			sp = getStoragePoolbyURI(conn, volDir);
			vol = sp.storageVolLookupByName(volName);
			return vol;
		} catch (LibvirtException e) {
			s_logger.debug("Faild to get vol path: " + e.toString());
			throw e;
		} finally {
			try {
				if (sp != null) {
					sp.free();
				}
			} catch (LibvirtException e) {

			}
		}
	}

	public StoragePool createFileBasedStoragePool(Connect conn,
			String localStoragePath, String uuid) {
		if (!(_storageLayer.exists(localStoragePath) && _storageLayer
				.isDirectory(localStoragePath))) {
			return null;
		}

		File path = new File(localStoragePath);
		if (!(path.canWrite() && path.canRead() && path.canExecute())) {
			return null;
		}

		StoragePool pool = null;

		try {
			pool = conn.storagePoolLookupByUUIDString(uuid);
		} catch (LibvirtException e) {

		}

		if (pool == null) {
			LibvirtStoragePoolDef spd = new LibvirtStoragePoolDef(poolType.DIR,
					uuid, uuid, null, null, localStoragePath);
			try {
				pool = conn.storagePoolDefineXML(spd.toString(), 0);
				pool.create(0);
			} catch (LibvirtException e) {
				if (pool != null) {
					try {
						pool.destroy();
						pool.undefine();
					} catch (LibvirtException e1) {
					}
					pool = null;
				}
				throw new CloudRuntimeException(e.toString());
			}
		}

		try {
			StoragePoolInfo spi = pool.getInfo();
			if (spi.state != StoragePoolState.VIR_STORAGE_POOL_RUNNING) {
				pool.create(0);
			}

		} catch (LibvirtException e) {
			throw new CloudRuntimeException(e.toString());
		}

		return pool;
	}

	private void getStats(LibvirtStoragePool pool) {
		Script statsScript = new Script("/bin/bash", s_logger);
		statsScript.add("-c");
		statsScript.add("stats=$(df --total " + pool.getLocalPath()
				+ " |grep total|awk '{print $2,$3}');echo $stats");
		final OutputInterpreter.OneLineParser statsParser = new OutputInterpreter.OneLineParser();
		String result = statsScript.execute(statsParser);
		if (result == null) {
			String stats = statsParser.getLine();
			if (stats != null && !stats.isEmpty()) {
				String sizes[] = stats.trim().split(" ");
				if (sizes.length == 2) {
					pool.setCapacity(Long.parseLong(sizes[0]) * 1024);
					pool.setUsed(Long.parseLong(sizes[1]) * 1024);
				}
			}
		}
	}

	@Override
	public KVMStoragePool getStoragePool(String uuid) {
		StoragePool storage = null;
		try {
			Connect conn = LibvirtConnection.getConnection();
			storage = conn.storagePoolLookupByUUIDString(uuid);

			if (storage.getInfo().state != StoragePoolState.VIR_STORAGE_POOL_RUNNING) {
				storage.create(0);
			}
			LibvirtStoragePoolDef spd = getStoragePoolDef(conn, storage);
			StoragePoolType type = null;
			if (spd.getPoolType() == LibvirtStoragePoolDef.poolType.NETFS
					|| spd.getPoolType() == LibvirtStoragePoolDef.poolType.DIR) {
				type = StoragePoolType.Filesystem;
			} else if (spd.getPoolType() == LibvirtStoragePoolDef.poolType.LOGICAL) {
				type = StoragePoolType.CLVM;
			}
			LibvirtStoragePool pool = new LibvirtStoragePool(uuid,
					storage.getName(), type, this, storage);
			pool.setLocalPath(spd.getTargetPath());

			if (pool.getType() == StoragePoolType.CLVM) {
				pool.setCapacity(storage.getInfo().capacity);
				pool.setUsed(storage.getInfo().allocation);
			} else {
				getStats(pool);
			}
			return pool;
		} catch (LibvirtException e) {
			throw new CloudRuntimeException(e.toString());
		}
	}

	@Override
	public KVMPhysicalDisk getPhysicalDisk(String volumeUuid,
			KVMStoragePool pool) {
		LibvirtStoragePool libvirtPool = (LibvirtStoragePool) pool;

		try {
			StorageVol vol = this.getVolume(libvirtPool.getPool(), volumeUuid);
			KVMPhysicalDisk disk;
			LibvirtStorageVolumeDef voldef = getStorageVolumeDef(libvirtPool
					.getPool().getConnect(), vol);
			disk = new KVMPhysicalDisk(vol.getPath(), vol.getName(), pool);
			disk.setSize(vol.getInfo().allocation);
			disk.setVirtualSize(vol.getInfo().capacity);
			if (voldef.getFormat() == null) {
				disk.setFormat(pool.getDefaultFormat());
			} else if (voldef.getFormat() == LibvirtStorageVolumeDef.volFormat.QCOW2) {
				disk.setFormat(KVMPhysicalDisk.PhysicalDiskFormat.QCOW2);
			} else if (voldef.getFormat() == LibvirtStorageVolumeDef.volFormat.RAW) {
				disk.setFormat(KVMPhysicalDisk.PhysicalDiskFormat.RAW);
			}
			return disk;
		} catch (LibvirtException e) {
			throw new CloudRuntimeException(e.toString());
		}

	}

	@Override
	public KVMStoragePool createStoragePool(String name, String host,
			String path, StoragePoolType type) {
		StoragePool sp = null;
		Connect conn = null;
		try {
			conn = LibvirtConnection.getConnection();
		} catch (LibvirtException e) {
			throw new CloudRuntimeException(e.toString());
		}

		try {
			sp = conn.storagePoolLookupByUUIDString(name);
			if (sp.getInfo().state != StoragePoolState.VIR_STORAGE_POOL_RUNNING) {
				sp.undefine();
				sp = null;
			}
		} catch (LibvirtException e) {

		}

		if (sp == null) {
			if (type == StoragePoolType.NetworkFilesystem) {
				sp = createNfsStoragePool(conn, name, host, path);
			} else if (type == StoragePoolType.SharedMountPoint
					|| type == StoragePoolType.Filesystem) {
				sp = CreateSharedStoragePool(conn, name, host, path);
			} else if (type == StoragePoolType.CLVM) {
				sp = createCLVMStoragePool(conn, name, host, path);
			}
		}

		try {
			StoragePoolInfo spi = sp.getInfo();
			if (spi.state != StoragePoolState.VIR_STORAGE_POOL_RUNNING) {
				sp.create(0);
			}

			LibvirtStoragePoolDef spd = getStoragePoolDef(conn, sp);
			LibvirtStoragePool pool = new LibvirtStoragePool(name,
					sp.getName(), type, this, sp);
			pool.setLocalPath(spd.getTargetPath());

			if (pool.getType() == StoragePoolType.CLVM) {
				pool.setCapacity(sp.getInfo().capacity);
				pool.setUsed(sp.getInfo().allocation);
			} else {
				getStats(pool);
			}
			return pool;
		} catch (LibvirtException e) {
			throw new CloudRuntimeException(e.toString());
		}

	}

	@Override
	public boolean deleteStoragePool(String uuid) {
		Connect conn = null;
		try {
			conn = LibvirtConnection.getConnection();
		} catch (LibvirtException e) {
			throw new CloudRuntimeException(e.toString());
		}

		StoragePool sp = null;

		try {
			sp = conn.storagePoolLookupByUUIDString(uuid);
		} catch (LibvirtException e) {
			return true;
		}

		try {
			sp.destroy();
			sp.undefine();
			sp.free();
			return true;
		} catch (LibvirtException e) {
			throw new CloudRuntimeException(e.toString());
		}
	}

	@Override
	public KVMPhysicalDisk createPhysicalDisk(String name, KVMStoragePool pool,
			PhysicalDiskFormat format, long size) {
		LibvirtStoragePool libvirtPool = (LibvirtStoragePool) pool;
		StoragePool virtPool = libvirtPool.getPool();
		LibvirtStorageVolumeDef.volFormat libvirtformat = null;
		if (format == PhysicalDiskFormat.QCOW2) {
			libvirtformat = LibvirtStorageVolumeDef.volFormat.QCOW2;
		} else if (format == PhysicalDiskFormat.RAW) {
			libvirtformat = LibvirtStorageVolumeDef.volFormat.RAW;
		}

		LibvirtStorageVolumeDef volDef = new LibvirtStorageVolumeDef(name,
				size, libvirtformat, null, null);
		s_logger.debug(volDef.toString());
		try {
			StorageVol vol = virtPool.storageVolCreateXML(volDef.toString(), 0);
			KVMPhysicalDisk disk = new KVMPhysicalDisk(vol.getPath(),
					vol.getName(), pool);
			disk.setFormat(format);
			disk.setSize(vol.getInfo().allocation);
			disk.setVirtualSize(vol.getInfo().capacity);
			return disk;
		} catch (LibvirtException e) {
			throw new CloudRuntimeException(e.toString());
		}
	}

	@Override
	public boolean deletePhysicalDisk(String uuid, KVMStoragePool pool) {
		LibvirtStoragePool libvirtPool = (LibvirtStoragePool) pool;
		try {
			StorageVol vol = this.getVolume(libvirtPool.getPool(), uuid);
			vol.delete(0);
			vol.free();
			return true;
		} catch (LibvirtException e) {
			throw new CloudRuntimeException(e.toString());
		}
	}

	@Override
	public KVMPhysicalDisk createDiskFromTemplate(KVMPhysicalDisk template,
			String name, PhysicalDiskFormat format, long size,
			KVMStoragePool destPool) {
		KVMPhysicalDisk disk = destPool.createPhysicalDisk(UUID.randomUUID()
				.toString(), format, template.getVirtualSize());

		if (format == PhysicalDiskFormat.QCOW2) {
			Script.runSimpleBashScript("qemu-img create -f "
					+ template.getFormat() + " -b  " + template.getPath() + " "
					+ disk.getPath());
		} else if (format == PhysicalDiskFormat.RAW) {
			Script.runSimpleBashScript("qemu-img convert -f "
					+ template.getFormat() + " -O raw " + template.getPath()
					+ " " + disk.getPath());
		}
		return disk;
	}

	@Override
	public KVMPhysicalDisk createTemplateFromDisk(KVMPhysicalDisk disk,
			String name, PhysicalDiskFormat format, long size,
			KVMStoragePool destPool) {
		return null;
	}

	@Override
	public List<KVMPhysicalDisk> listPhysicalDisks(String storagePoolUuid,
			KVMStoragePool pool) {
		LibvirtStoragePool libvirtPool = (LibvirtStoragePool) pool;
		StoragePool virtPool = libvirtPool.getPool();
		List<KVMPhysicalDisk> disks = new ArrayList<KVMPhysicalDisk>();
		try {
			String[] vols = virtPool.listVolumes();
			for (String volName : vols) {
				KVMPhysicalDisk disk = this.getPhysicalDisk(volName, pool);
				disks.add(disk);
			}
			return disks;
		} catch (LibvirtException e) {
			throw new CloudRuntimeException(e.toString());
		}
	}

	@Override
	public KVMPhysicalDisk copyPhysicalDisk(KVMPhysicalDisk disk, String name,
			KVMStoragePool destPool) {
		KVMPhysicalDisk newDisk = destPool.createPhysicalDisk(name,
				disk.getVirtualSize());
		String sourcePath = disk.getPath();
		String destPath = newDisk.getPath();

		Script.runSimpleBashScript("qemu-img convert -f " + disk.getFormat()
				+ " -O " + newDisk.getFormat() + " " + sourcePath + " "
				+ destPath);
		return newDisk;
	}

	@Override
	public KVMStoragePool getStoragePoolByUri(String uri) {
		URI storageUri = null;

		try {
			storageUri = new URI(uri);
		} catch (URISyntaxException e) {
			throw new CloudRuntimeException(e.toString());
		}

		String sourcePath = null;
		String uuid = null;
		String sourceHost = "";
		StoragePoolType protocal = null;
		if (storageUri.getScheme().equalsIgnoreCase("nfs")) {
			sourcePath = storageUri.getPath();
			sourcePath = sourcePath.replace("//", "/");
			sourceHost = storageUri.getHost();
			uuid = UUID.randomUUID().toString();
			protocal = StoragePoolType.NetworkFilesystem;
		}

		return createStoragePool(uuid, sourceHost, sourcePath, protocal);
	}

	@Override
	public KVMPhysicalDisk getPhysicalDiskFromURI(String uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public KVMPhysicalDisk createDiskFromSnapshot(KVMPhysicalDisk snapshot,
			String snapshotName, String name, KVMStoragePool destPool) {
		return null;
	}

	@Override
	public boolean refresh(KVMStoragePool pool) {
		LibvirtStoragePool libvirtPool = (LibvirtStoragePool) pool;
		StoragePool virtPool = libvirtPool.getPool();
		try {
			virtPool.refresh(0);
		} catch (LibvirtException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean deleteStoragePool(KVMStoragePool pool) {
		LibvirtStoragePool libvirtPool = (LibvirtStoragePool) pool;
		StoragePool virtPool = libvirtPool.getPool();
		try {
			virtPool.destroy();
			virtPool.undefine();
			virtPool.free();
		} catch (LibvirtException e) {
			return false;
		}

		return true;
	}

}
