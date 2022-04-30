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

## License

GPL-2.0
