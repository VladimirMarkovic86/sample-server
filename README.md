# Sample server

Sample server project implements back-end part of sample project, and is running on server-lib project

### Installing

Clone project from git by executing:

```
git clone git@github.com:VladimirMarkovic86/sample-server.git

or

git clone https://github.com/VladimirMarkovic86/sample-server.git
```

After that execute command:

```
cd sample-server
```

Add following line in hosts file:

```
127.0.0.1 sample
```

and run project with this command:

```
lein run
```

By default project listens on port 1603, so you can make requests on https://sample:1603 address.

**For purpose of making requests sample-client was made and you should start up sample-client also.**

**MongoDB server should also be up and running and its URI link should be set in PROD_MONGODB environment variable**

## Authors

* **Vladimir Markovic** - [VladimirMarkovic86](https://github.com/VladimirMarkovic86)

## License

This project is licensed under the Eclipse Public License 1.0 - see the [LICENSE](LICENSE) file for details

