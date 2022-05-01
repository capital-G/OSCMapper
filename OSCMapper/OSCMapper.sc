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

	getValue {
		^value;
	}

	asNdef {
		ndef = ndef ? Ndef(name.asSymbol, {
			\val.kr(0.0, lag: \lag.kr(lag))
		});
		^ndef;
	}

	asBus {
		bus = bus ? Bus.control(
			server: Server.default,
			numChannels: 1
		);
		^bus;
	}

	asPdefn {
		pdefn = pdefn ? Pdefn(name.asSymbol, 0.0);
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

		prCallbacks ++ [callback].do({|cb|
			cb.(value);
		});
	}
}

OSCMapperFader : OSCMapperElement {
	printOn { | stream |
		stream << "OSCMapperFader(altName: " << altName << ", defaultValue: " << defaultValue << ")";
	}

}

OSCMapperPush : OSCMapperElement {
	var pressCallback;
	var releaseCallback;

	*new { |altName, pressCallback, releaseCallback|
		^super.newCopyArgs().initPush(
			pressCallback,
			releaseCallback,
		);
	}

	initPush {|pressCallback_, releaseCallback_|
		this.initTouchElement();

		// copy elements
		pressCallback = pressCallback_;
		releaseCallback = releaseCallback_;

		if(pressCallback.notNil, {
			this.onPress(pressCallback)
		});
		if(releaseCallback.notNil, {
			this.onRelease(releaseCallback)
		});
	}

	onPress { |callback |
		prCallbacks.add({|value|
			if(value==1.0, callback);
		});
	}

	onRelease { |callback|
		prCallbacks.add({|value|
			if(value==0.0, callback)
		});
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
	var <>address;

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
			altName: "%_x".format(altName).asSymbol,
			defaultValue: defaultValueX,
			transformer: transformerX,
			callback: callbackX,
			lag: lagX,
		);
		y = OSCMapperFader(
			altName: "%_y".format(altName).asSymbol,
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

	printOn { | stream |
		stream << "OSCMapperXY(altName: " << altName << ")";
	}
}

OSCMapper {
	var name;
	var layout;
	var <l;

	classvar oscListener;
	classvar listenerActive;
	classvar printAddress;
	classvar <>isLearning;
	classvar <>learnCapture;

	*new { |name, layout|
		^super.newCopyArgs(
			name,
			layout,
		).init;
	}

	*initClass {
		oscListener = ();
		printAddress = false;
		listenerActive = false;
		isLearning = false;
		learnCapture = ();
	}

	*initListener {
		if(listenerActive.not, {
			"add osc listener now".postln;
			// todo make this a lambda funcion?
			thisProcess.addOSCRecvFunc({|msg|
				OSCMapper.listenOnOSC(msg)
			});
			listenerActive = true;
		});
	}

	*mixer2 {}


	init {
		OSCMapper.initListener;

		l = ();

		layout.pairsDo({|address, t|
			t.address = address;
			oscListener[address.asSymbol] = t;
			l[address.asSymbol] = t;
			// also add the controller under its alternative name if provided
			if(t.altName.notNil, {
				l[t.altName] = t;
			});
		});
	}

	at {|address|
		^l[address];
	}


	*listenOnOSC {|msg|
		var address = msg[0].asSymbol;

		var addressesToIgnore = [
			"/status",
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
		oscListener.pairsDo({|address, element|
			if(msg[0].asSymbol == address.asSymbol, {
				element.update(*msg[1..]);
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

	clear {}

	clearAll {}

	*prCreateOscListener {
		thisProcess.addOSCRecvFunc(OSCMapper.oscListener);
	}
}
