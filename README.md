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
3. In atnos-eff, `last` of `Eff` is handled like `try-catch-finally` logic of `MonadError` but
    - the last logic is not the same of `try-catch-finally` because the last logic should be actually done in the last
      if some `Eff`s are flatmapped
    - on the other hand, the error handling should be able to be applied to a specific `Eff` data, and it doesn't need to be done in the final

This repository is a PoC of (2).

Discussions are very welcome. Thank you for the reading!

## How to use

You can try it out by the following command.

```console
./sbt test
```

## Acknowledgments

I would like to thank [@halcat0x15a](https://github.com/halcat0x15a) and [@xuwei-k](https://github.com/xuwei-k) for many advices.
But if there could be any mistakes in this repository, they were nothing to do with that.

