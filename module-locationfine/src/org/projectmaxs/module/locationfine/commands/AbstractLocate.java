/*
    This file is part of Project MAXS.

    MAXS and its modules is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MAXS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MAXS.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.projectmaxs.module.locationfine.commands;

import org.projectmaxs.module.locationfine.ModuleService;
import org.projectmaxs.module.locationfine.service.LocationService;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.SubCommand;

import android.content.Context;
import android.content.Intent;

public abstract class AbstractLocate extends SubCommand {

	protected static final Log LOG = Log.getLog();

	public AbstractLocate(String name) {
		this(name, false);
	}

	public AbstractLocate(String name, boolean isDefaultWithoutArguments) {
		super(ModuleService.LOCATE, name, isDefaultWithoutArguments);
	}

	static void startLocationServiceNotSticky(Context context, Command command) {
		locationService(LocationService.START_SERVICE_NOT_STICKY, context, command);
	}

	static void startLocationService(Context context, Command command) {
		locationService(LocationService.START_SERVICE, context, command);
	}

	static void stopLocationService(Context context, Command command) {
		locationService(LocationService.STOP_SERVICE, context, command);
	}

	static private void locationService(String action, Context context, Command command) {
		Intent intent = new Intent(context, LocationService.class);
		intent.setAction(action);
		intent.putExtra(GlobalConstants.EXTRA_COMMAND, command);
		context.startService(intent);
	}
}
