import random
import sqlite3
import string

import cherrypy

DB = 'my.db'


@cherrypy.expose
class IdGeneratorService(object):
	def GET(self):
		cherrypy.session['id'] = cherrypy.session.id
		with sqlite3.connect(DB) as c:
			r = c.execute('SELECT value FROM user_string WHERE session_id=?', [cherrypy.session.id])
			return r.fetchone()

	def POST(self, length=8):
		with sqlite3.connect(DB) as c:
			cherrypy.session['user_id'] = ''.join(random.sample(string.hexdigits, int(length)))
			c.execute('INSERT INTO user_string VALUES (?, ?)', [cherrypy.session.id, cherrypy.session['id']])

	def PUT(self, string):
		cherrypy.session['user_id'] = string

	def DELETE(self):
		cherrypy.session['user_id'] = None

def setup_database():
	with sqlite3.connect(DB) as c:
		c.execute('CREATE TABLE user_string (session_id, value)')

def cleanup_database():
	with sqlite3.connect(DB) as c:
		c.execute('DROP TABLE user_string')


if __name__ == '__main__':
	conf = {
		'/' : {
			'request.dispatch': cherrypy.dispatch.MethodDispatcher(),
			'tools.sessions.on': True,
		}
	}

	cherrypy.engine.subscribe('start', setup_database)
	cherrypy.engine.subscribe('stop', cleanup_database)
	cherrypy.quickstart(IdGeneratorService(), '/', conf)

