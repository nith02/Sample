#! /usr/bin/env python

def basic(n, m=2):
	""" Basic python syntax.

	Basic python syntax testing.
	"""
	i = 0
	while i < 10:
		i += 1

	if i == 5:
		print 'five'
	elif i == 10:
		print 'ten'
	else:
		print 'more'

	names = ['John', 'Mary', 'Tom']
	for name in names:
		print name, len(name)

	for num in range(2):
		pass
	else:
		print 'loop end'

	n += 1
	print n, m

def func1(a, l=None):
	if l is None:
		l = []
	l.append( a )
	print l

def func2(kind, *name, **names):
	print 'do you have any ', kind, '?'
	print 'name ', name
	print 'names ', names

def func3( format, *args ):
	print format % args

def func4(n):
	return lambda x, incr=n: x+incr

n = 1
basic(n)
print n
print basic.__doc__

func1(1)
func1(2)
func2( 'game', 'game1', '100', game1='game1', game2='game2')
func3( 'a = %d b = %s', 1, 'name')
print func4(6)(2)
