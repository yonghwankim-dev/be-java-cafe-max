insert into users(USERID, PASSWORD, NAME, EMAIL)
select USERID, PASSWORD, NAME, EMAIL
from CSVREAD('classpath:db\h2\sampleUser.csv', 'USERID,PASSWORD,NAME,EMAIL')
limit 100;

insert into question(TITLE, CONTENT, USERID)
select TITLE, CONTENT, USERID
from CSVREAD('classpath:db\h2\sampleQuestion.csv', 'TITLE,CONTENT,USERID')
limit 100;

insert into comment(CONTENT, QUESTIONID, USERID)
select CONTENT, QUESTIONID, USERID
from CSVREAD('classpath:db\h2\sampleComment.csv', 'CONTENT,QUESTIONID,USERID')
limit 100;
