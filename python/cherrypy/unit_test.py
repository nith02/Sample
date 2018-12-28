import requests

s = requests.Session()

r = s.get('http://127.0.0.1:8080/')
print (r.status_code)

r = s.post('http://127.0.0.1:8080/')
print (r.status_code)

r = s.get('http://127.0.0.1:8080/')
print (r.status_code, r.text)

r = s.put('http://127.0.0.1:8080/', params={'string': 'hello'})
print (r.status_code)

r = s.delete('http://127.0.0.1:8080/')
print (r.status_code)
