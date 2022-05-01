# OSC Mapper

An abstraction layer for OSC controllers and SuperCollider.
Allows learning of controls and much more.

## Installation

```supercollider
// install the quark
Quarks.install("https://github.com/capital-G/OSCMapper.git");
// restart the interpreter so the new classes are available
thisProcess.recompile;
```

## Quickstart

### Learn OSC commands

Based on the input OSCMapper receives it will try to guess what kind of interface the address represents.

```supercollider
// turn OSC Mapper into learning mode
OSCMapper.learn;
// move (all) controllers
// when finished create a new controller
m = OSCMapper.finishLearn(\myController);

// now we can simply use the controller values in an ndef
(
Ndef(\mySound, {
    var sig = Saw.ar(LFDNoise1.kr(2.5!2).exprange(400, 410));
    sig = sig * m['/1/fader1'].asNdef;
    sig;
}).play;
)
Ndef(\mySound).clear(1.0);
```

### Static layout

If you want to establish a fixed layout you can also provide a static layout.

```supercollider
(
o = OSCMapper(\myLayout, (
    '/1/fader1': OSCMapperFader(
        altName: \fader1,
        defaultValue: 0.5,
        transformer: linlin(_, 0.0, 1.0, 0.5, 10.0),
        callback: {|v| ["received a value", v].postln;},
        lag: 0.5,
    ),
    '/1/xy1': OSCMapperXY(
        altName: \touchPanel
    ),
));

Ndef(\mySound, {
    SinOscFB.ar(
        freq: LFDNoise1.kr(o[\fader1].asNdef!2).exprange(200, 400),
        feedback: o[\touchPanel].x.asNdef,
    ) * o[\touchPanel].y.asNdef;
}).play;
)
```

## License

GPL-2.0
