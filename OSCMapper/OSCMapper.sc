OSCMapperElement {
	var <altName;
	var <>defaultValue;
	var <>transformer;
	var <>callback;
	var <lag;

	// private stuff
	var <name;
	var address;
	var <ndef;
	var <bus;
	var <pdefn;
	var <value;
	var prCallbacks;

	*new { |altName, defaultValue=0.0, transformer, callback, lag=0.2|
		^super.newCopyArgs(
			altName,
			defaultValue,
			transformer,
			callback,
			lag,
		).initTouchElement;
	}

	initTouchElement {
		// TODO typecheck on callbacks
		callback = callback ? {};
		prCallbacks = [];
		transformer = transformer ? {|i| i};
		value = defaultValue;
	}

	address_ { |newAddress|
		address = newAddress;
		name = "OSCMapper_%".format(address);
	}

	lag_ { |newLag|
		lag = newLag;
		if(ndef.notNil, {
			ndef.set(\lag, lag);
		});
	}

	asNdef {
		ndef = ndef ? Ndef(name.asSymbol, {
			\val.kr(value, lag: \lag.kr(lag))
		});
		^ndef;
	}

	asBus {
		bus = bus ? Bus.control(
			server: Server.default,
			numChannels: 1
		);
		bus.set(value);
		^bus;
	}

	asPdefn {
		pdefn = pdefn ? Pdefn(name.asSymbol, value);
		^pdefn;
	}

	update {|...newValues|
		value = transformer.(newValues[0]);

		if(bus.notNil, {
			bus.set(value);
		});

		if(ndef.notNil, {
			ndef.set(\val, value);
		});

		if(pdefn.notNil, {
			Pdefn(name.asSymbol, value);
		});

		(prCallbacks ++ [callback]).do({|cb|
			cb.value(value);
		});
	}
}

OSCMapperFader : OSCMapperElement {
	printOn { | stream |
		stream << "OSCMapperFader(altName: " << altName << ", defaultValue: " << defaultValue << ")";
	}

}

OSCMapperPush : OSCMapperElement {
	var <>onPress;
	var <>onRelease;
	var <>onPush;

	*new { |altName, onPress, onRelease, onPush|
		^super.newCopyArgs().initPush(
			onPress,
			onRelease,
			onPush,
		);
	}

	initPush {|pressCallback_, releaseCallback_, pushCallback_|
		this.initTouchElement();

		onPress = pressCallback_ ? {};
		onRelease = releaseCallback_ ? {};
		onPush = pushCallback_ ? {};
		prCallbacks = prCallbacks ++ [{|v| this.checkPushCallbacks(v)}];
	}

	checkPushCallbacks {|value|
		if(value == 1.0, {this.onPress.value(value)});
		if(value == 0.0, {this.onRelease.value(value)});
		this.onPush.value(value);
	}

	printOn { | stream |
		stream << "OSCMapperPush(altName: " << altName << ")";
	}
}

OSCMapperXY {
	var <altName;
	var defaultValueX;
	var defaultValueY;
	var transformerX;
	var transformerY;
	var callbackX;
	var callbackY;
	var lagX;
	var lagY;

	var <x;
	var <y;
	var <address;

	*new { |altName, defaultValueX=0.0, defaultValueY=0.0 transformerX, transformerY, callbackX, callbackY, lagX=0.2, lagY=0.2|
		^super.newCopyArgs(
			altName,
			defaultValueX,
			defaultValueY,
			transformerX,
			transformerY,
			callbackX,
			callbackY,
			lagX,
			lagY,
		).initXY;
	}

	initXY {
		x = OSCMapperFader(
			altName: altName.isNil.if({nil}, {"%_x".format(altName).asSymbol}),
			defaultValue: defaultValueX,
			transformer: transformerX,
			callback: callbackX,
			lag: lagX,
		);
		y = OSCMapperFader(
			altName: altName.isNil.if({nil}, {"%_y".format(altName).asSymbol}),
			defaultValue: defaultValueY,
			transformer: transformerY,
			callback: callbackY,
			lag: lagY,
		);
	}

	update {|...newValues|
		x.update(newValues[0]);
		y.update(newValues[1]);
	}

	address_ { |newAddress|
		address = newAddress;
		x.address = "%_x".format(address).asSymbol;
		y.address = "%_y".format(address).asSymbol;
	}

	printOn { | stream |
		stream << "OSCMapperXY(altName: " << altName << ")";
	}
}

OSCMapperAccXYZ {
	var <altName;
	var defaultValueX;
	var defaultValueY;
	var defaultValueZ;
	var transformerX;
	var transformerY;
	var transformerZ;
	var callbackX;
	var callbackY;
	var callbackZ;
	var lagX;
	var lagY;
	var lagZ;

	var <x;
	var <y;
	var <z;
	var <address;

	*new { |
		altName,
		defaultValueX,
		defaultValueY,
		defaultValueZ,
		transformerX,
		transformerY,
		transformerZ,
		callbackX,
		callbackY,
		callbackZ,
		lagX=0.2,
		lagY=0.2,
		lagZ=0.2
		|
		^super.newCopyArgs(
			altName,
			defaultValueX,
			defaultValueY,
			defaultValueZ,
			transformerX,
			transformerY,
			transformerZ,
			callbackX,
			callbackY,
			callbackZ,
			lagX,
			lagY,
			lagZ,
		).initAccXYZ;
	}

	initAccXYZ {
		x = OSCMapperFader(
			altName: altName.isNil.if({nil}, {"%_x".format(altName).asSymbol}),
			defaultValue: defaultValueX,
			transformer: transformerX,
			callback: callbackX,
			lag: lagX,
		);

		y = OSCMapperFader(
			altName: altName.isNil.if({nil}, {"%_y".format(altName).asSymbol}),
			defaultValue: defaultValueY,
			transformer: transformerY,
			callback: callbackY,
			lag: lagY,
		);

		z = OSCMapperFader(
			altName: altName.isNil.if({nil}, {"%_z".format(altName).asSymbol}),
			defaultValue: defaultValueZ,
			transformer: transformerZ,
			callback: callbackZ,
			lag: lagZ,
		);
	}

	update { |...newValues|
		x.update(newValues[0]);
		y.update(newValues[1]);
		z.update(newValues[2]);
	}

	address_ { |newAddress|
		address = newAddress;
		x.address = "%_x".format(address).asSymbol;
		y.address = "%_y".format(address).asSymbol;
		z.address = "%_z".format(address).asSymbol;
	}

	printOn { | stream |
		stream << "OSCMapperAccXYZ(altName: " << altName << ")";
	}
}

OSCMapperKeyValue : OSCMapperElement {
	var d;

	at { |key|
		^d[key.asSymbol];
	}

	update { |...newValues|
		d = d ? ();
		newValues.pairsDo({|key, value|
			var element = d[key.asSymbol];
			if(element.isNil, {
				element = OSCMapperFader();
				element.address = "%_%".format(address, key).asSymbol;
				d[key.asSymbol] = element;
				// necessary to update the proper element?
				element = d[key.asSymbol];
			});
			element.update(value);
		});
	}

	printOn { | stream |
		stream << "OSCMapperKeyValue(altName: " << altName << ")";
	}
}

OSCMapperArray : OSCMapperElement {
	var a;

	at { |index|
		^a[index];
	}

	update { |...newValues|
		a = a ? [];
		if(a.size != newValues.size, {
			a = newValues.size.collect({|i|
				var element = OSCMapperFader();
				element.address = "%_%".format(address, i).asSymbol;
				element;
			});
		});
		newValues.do({|value, i|
			a[i].update(value);
		});
	}

	printOn { | stream |
		stream << "OSCMapperArray(altName: " << altName << ")";
	}
}

OSCMapper {
	var name;
	var initLayout;
	var <port;

	var <layout;

	classvar listenerActive;
	classvar printAddress;
	classvar <>isLearning;
	classvar <>learnCapture;
	classvar <all;

	*new { |name, layout, port|
		var res = all[name.asSymbol];
		if(res.notNil, {
			if(layout.notNil, {
				res.layout = layout;
			});
		}, {
			if(port.notNil, {thisProcess.openUDPPort(port)});
			res = super.newCopyArgs(
				name,
				layout,
				port,
			).init;
			all[name.asSymbol] = res;
		});

		^res;
	}

	*initClass {
		printAddress = false;
		listenerActive = false;
		isLearning = false;
		learnCapture = ();
		all = ();
	}

	*initListener {
		if(listenerActive.not, {
			"Init OSC listener for OSCMapper".postln;
			thisProcess.addOSCRecvFunc({|msg, time, replyAddr, recvPort|
				OSCMapper.listenOnOSC(msg, time, replyAddr, recvPort);
			});
			listenerActive = true;
		});
	}

	*mix2 {|name = \mix2, port|
		var l = ();
		(1..3).do({|i| l["/1/fader%".format(i).asSymbol] = OSCMapperFader()});
		(1..4).do({|i| l["/1/push%".format(i).asSymbol] = OSCMapperPush()});
		(1..6).do({|i| l["/1/rotary%".format(i).asSymbol] = OSCMapperFader()});
		(1..4).do({|i| l["/1/toggle%".format(i).asSymbol] = OSCMapperPush()});
		(1..2).do({|i| (1..16).do({|j| l["/2/multifader%/%".format(i, j).asSymbol] = OSCMapperFader()})});
		(1..2).do({|i| l["/3/xy%".format(i).asSymbol] = OSCMapperXY()});
		l['/accxyz'] = OSCMapperAccXYZ();
		^OSCMapper(name: name.asSymbol, layout: l, port: port);
	}

	*mix2iPad {|name=\mix2iPad, port|
		var l = ();
		(1..12).do({|i| l["/1/rotary%".format(i).asSymbol] = OSCMapperFader()});
		(1..5).do({|i| l["/1/fader%".format(i).asSymbol] = OSCMapperFader()});
		(1..16).do({|i| l["/1/toggle%".format(i).asSymbol] = OSCMapperPush()});
		(1..12).do({|i| l["/1/push%".format(i).asSymbol] = OSCMapperPush()});
		(1..2).do({|i| l["/1/xy%".format(i).asSymbol] = OSCMapperXY()});
		(1..2).do({|i| (1..5).do({|j| (1..5).do({|k| l["/1/multitoggle%/%/%".format(i, j, k).asSymbol] = OSCMapperPush() }) })});
		(1..2).do({|i| (1..3).do({|j| l["/1/multitoggle3/%/%".format(i, j).asSymbol] = OSCMapperPush() }) });
		(1..2).do({|i| (1..16).do({|j| l["/2/multifader%/%".format(i, j).asSymbol] = OSCMapperFader()})});
		(1..8).do({|i| l["/3/xy%".format(i).asSymbol] = OSCMapperXY()});
		l['/accxyz'] = OSCMapperAccXYZ();
		^OSCMapper(name: name.asSymbol, layout: l, port: port);
	}

	init {
		OSCMapper.initListener;
		layout = layout ? ();
		this.layout_(initLayout);
	}

	at {|address|
		var res;
		address = address.asSymbol;

		res = layout[address];
		// maybe the address is an alternative name?
		if(res.isNil, {
			layout.pairsDo({|addr, element|
				if(address == element.altName.asSymbol, {
					res = element;
				});
			});
		});
		^res;
	}

	layout_ { |newLayout|
		newLayout.pairsDo({|address, t|
			address = address.asSymbol;
			// set the address within the mapper element
			// as this is necessary to generate
			// an unique name for e.g. an ndef
			t.address = address;
		});
		layout = newLayout;
	}


	*listenOnOSC {|msg, time, replyAddr, recvPort|
		var address = msg[0].asSymbol;

		var addressesToIgnore = [
			"/localhost",
			"/status",
			"/n_",
			"/synced",
			"/d_",
			"/done",
			"g_queryTree",
		];
		if(addressesToIgnore.any({|a|
			address.asString.beginsWith(a)
		}), {
			^this;
		});

		if(printAddress, {
			"Received on address \"%\" (values %)".format(msg[0], msg[1..]).postln;
		});

		// capture knob movements if isLearning
		if(isLearning, {
			var values = learnCapture[msg[0].asSymbol] ? [];
			learnCapture[msg[0].asSymbol] = values.asSet.add(msg[1..]);
		});

		// run callbacks for address
		all.pairsDo({|name, mapper|
			var portMatch = if(mapper.port.isNil, {
				true;
			}, {
				recvPort == mapper.port;
			});
			if(portMatch, {
				mapper.layout.pairsDo({|address, element|
					if(msg[0].asSymbol == address.asSymbol, {
						element.update(*msg[1..]);
					});
				});
			});
		});
	}

	*echo {|echoBool=true|
		OSCMapper.initListener;
		printAddress = echoBool;
	}

	*learn {
		if(isLearning, {
			"You are already learning, use \"OSCMapper.finishLearn\" to stop current running learning process".postln;
			^this;
		});
		this.initListener();
		learnCapture = ();
		isLearning = true;
	}

	*finishLearn {|name|
		var r = ();
		if(isLearning.not, {
			"Run \"OSCMapper.learn\" first to start learning".postln;
			^this;
		});
		isLearning = false;

		// sort this so the print is nicer
		learnCapture.asSortedArray.do({|captures|
			var address = captures[0];
			var rawValues = captures[1];
			var values = rawValues.asArray.flatten(1).asSet;
			var t = case
			{ values == Set[] } { \page }
			{ address.asSymbol == '/accxyz' } { OSCMapperAccXYZ() }
			{ values == Set[0.0, 1.0]} {
				if(address.asString.contains("toggle"), {
					// this is a toggle which is 1 or 0
					OSCMapperPush()
				}, {
					// jumps to 1 on push and back on release
					OSCMapperPush();
				});
			}
			{ (values == Set[1.0]).or(values == Set[0.0]) } { OSCMapperPush()}
			{ rawValues.asArray[0].size > 1 } { OSCMapperXY() }
			{ values.asArray.size > 1 } { OSCMapperFader() }
			{ true } { \unknown };

			if(t.class == Symbol, {
				// unsupported
			}, {
				r[address.asSymbol] = t;
			});
			"'%': %,".format(address, t).postln;
		});
		learnCapture = ();
		^this.new(name, r);
	}

	clear {
		/*
		l.pairsDo({|address, element|
			oscListener[address.asSymbol] = nil;
		});
		*/
		all[name.asSymbol] = nil;
		layout = ();
	}

	*clearAll {
		all.pairsDo({|name, mapper|
			mapper.clear;
		});
	}

	*prCreateOscListener {
		thisProcess.addOSCRecvFunc(OSCMapper.oscListener);
	}
}
