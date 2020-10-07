Eff interpreter implementation for atnos-eff last action 
===========================================================

![CI](https://github.com/y-yu/atnos-eff-last-action/workflows/CI/badge.svg)


## Abstract

`Eff` from [atnos-eff](https://github.com/atnos-org/eff) has a function `addLast`
which allows us to register the side-effects we want to execute at the end.
But I think the implementation has some issues:

1. This feature is probably NOT described [original paper](http://okmij.org/ftp/Haskell/extensible/more.pdf)
    - so we cannot find out what is the right behavior of this😇
2. And this can be done by the original Eff idea without special modification for `Eff` data structure

This repository is a PoC of (2).

Discussions are very welcome. Thank you for the reading!

## How to use

You can try it out by the following command.

```console
./sbt test
```

## Acknowledgments

I would like to thank @halcat0x15a and @xuwei-k for many advices.
But if there could be any mistakes in this repository, they were nothing to do with that.

