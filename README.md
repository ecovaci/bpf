# bpf
### Basic Proxy Facade for Kerberos

BPF is a HTTP(s) proxy server facade that allows applications to authenticate through a Kerberos authenticated proxy server, typically used in corporate environments, 
without having to deal with the actual handshake.

A lot of software applications have problems when dealing with an authenticated proxy server's protocol. BPF sits between the corporate proxy and applications and offloads the authentication and the proxy's protocol, acting as a facade. This way, the software application will only have to deal with a basic proxy with no authentication.

An example of such facade for NTLM proxies is [CNTLM](http://cntlm.sourceforge.net/)

**Also, please give stars if you find this application useful and visit** [this page](https://eugencovaciq.wordpress.com/2018/05/24/basic-proxy-facade-for-kerberos/) **for feedback.**

### Instalation

BPF is a Java application, therefore you'll have to install a Java Runtime Environment version 8. See for example [Oracle Java SE Runtime Environment 8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html). If your OS is Windows, BPF comes with a release that includes a Java environment, so you don't have to install anything.

The installation is pretty basic, just unzip the content of the released archive then, on Windows OS double click on `launch.bat` file. On UNIX-based system, run:

`$./launch.sh`

Finally, I managed to get rid of the _krb5.conf_ configuration file from _config_ directory, which is now optional (although recommended). The _krb5.conf_ configuration file is a standard configuration file for Kerberos clients. To get the Kerberos KDC, the application issues the following command:

```
nslookup -type=srv _kerberos._tcp.REALM
```
where the realm is a Microsoft Windows domain name. I only managed to test it on Windows therefore testing on Unix systems would be highly appreciated.

### Configuration

Here is the main window:

![the main window](https://docs.google.com/uc?export=download&id=1HNpaYmOASk01QiFpIF8l-nM5ZPBx1FMy)

If the font is too big or too small use **CTRL -** or **CTRL +** to decrease/increase it.

Fill in all the fields and press _Start_ button. Now the proxy facade is up.

To test it, open a browser, let's say Firefox and configure proxy like this:

![firefox](https://docs.google.com/uc?export=download&id=1T18McN2oy4NPrIMtwS9CHlsYXz4KJi7T)

Now you should be able to access any URL without Firefox asking for credentials.


### TODO

   - ~~Get the domain automaticaly.~~
   - Debug mode.

### Feedback

Any feedback or suggestions are welcome. 
It is hosted with an Apache 2.0 license so issues, forks and PRs are most appreciated.

For comments please use [this page](https://eugencovaciq.wordpress.com/2018/05/24/basic-proxy-facade-for-kerberos/)


