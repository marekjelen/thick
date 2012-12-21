require 'socket'
require File.expand_path('../helper', __FILE__)

describe "Thick server" do

  it 'should respond to simple GET' do
    s = TCPSocket.new('localhost', 9292)
    s.puts(<<REQ)
GET / HTTP/1.1
Host: localhost:9292
Connection: keep-alive
Cache-Control: max-age=0
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.101 Safari/537.11
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
DNT: 1
Accept-Encoding: gzip,deflate,sdch
Accept-Language: en-US,en;q=0.8
Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3

REQ
    buffer = ""
    while tmp = s.gets
      buffer << tmp
    end
    s.close
    buffer.should == "HTTP/1.1 200 OK\r\nX-Frame-Options: sameorigin\r\nX-XSS-Protection: 1; mode=block\r\nContent-Type: text/html;charset=utf-8\r\nContent-Length: 2\r\n\r\nHi"
  end

  it 'should echo POSTed data back' do
    s = TCPSocket.new('localhost', 9292)
    s.puts(<<REQ)
POST /echo HTTP/1.1
Host: localhost:9292
Connection: keep-alive
Cache-Control: max-age=0
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_2) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.101 Safari/537.11
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
DNT: 1
Accept-Encoding: gzip,deflate,sdch
Accept-Language: en-US,en;q=0.8
Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.3
Content-Length: 21

This is a sample text
REQ
    buffer = ""
    while tmp = s.gets
      buffer << tmp
    end
    s.close
    buffer.should == "HTTP/1.1 200 OK\r\nX-Frame-Options: sameorigin\r\nX-XSS-Protection: 1; mode=block\r\nContent-Type: text/html;charset=utf-8\r\nContent-Length: 21\r\n\r\nThis is a sample text"
  end

end
