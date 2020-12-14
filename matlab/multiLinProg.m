fname = 'totalserver_10_trial_0.txt';
val = jsondecode(fileread(fname));
n = val.config.totalJobs;
m = val.config.totalServers;
t = val.config.totalDuration;
wn = val.config.maxWorkload;
a = zeros(1, n);
d = zeros(1, n);
c = zeros(1, n);
for k = 1:t
    a(k) = val.jobs(k).arrivalTime+1;
    d(k) = val.jobs(k).deadline+1;
    c(k) = val.jobs(k).workload;
end
A = sparse(m*t + n*t + n + 2*n*m*(t-1), n*m*t + n + n*m*(t-1));
B = horzcat(ones(1, m*t + n*t), -c, zeros(1, 2*n*m*(t-1)));
for k = 1:m*t
    tmp = zeros(m*t,n);
    tmp(k,:) = ones(1, n);
    A(k, 1:n*m*t) = reshape(tmp,1,[]);
end
fprintf("phase1\n");
for o = 1:n
    for k = 1:t
        tmp = zeros(t,m);
        tmp(k,:) = ones(1, m);
        A(m*t+(o-1)*t+k, (o-1)*m*t+1:o*m*t) = reshape(tmp,1,[]);
    end
end
fprintf("phase2\n");
for k = 1:n
    tmp = zeros(t, m, n);
    tmp(a(k):d(k),:,k) = -ones(d(k)-a(k)+1,m,1);
    tmp2 = zeros(1, n);
    tmp2(k) = -1;
    A((m+n)*t + k, 1:n*m*t+n) = horzcat(reshape(tmp,1,[]),tmp2);
end
fprintf("phase3\n");
for k = 0:(n*m-1)
    for l = 1:t-1
        A((m+n)*t+n + k*(t-1)+l, k*t+l) = 1;
        A((m+n)*t+n + k*(t-1)+l, k*t+l+1) = -1;
        A((m+n)*t+n + k*(t-1)+l, n*t+n+k*(t-1)+l) = -1;
        A((m+n)*t+n + n*m*(t-1) + k*(t-1)+l, k*t+l) = -1;
        A((m+n)*t+n + n*m*(t-1) + k*(t-1)+l, k*t+l+1) = 1;
        A((m+n)*t+n + n*m*(t-1) + k*(t-1)+l, n*t+n+k*(t-1)+l) = -1;
    end
end
fprintf("start\n");
lb = zeros(1, n*m*t + n + n*m*(t-1));
ub = horzcat(ones(1, n*m*t), c, ones(1, n*m*(t-1)));
options = optimoptions('linprog','Display','none');
f = horzcat(0.01*ones(1, n*m*t), ones(1, n), 0.00000001*ones(1, n*m*(t-1)));
[x, fval] = linprog(f,A,B,[],[],lb,ub,options);
prev = -1;
k = 0;
fprintf("%d: %f y=%d\n", k, fval, sum(x(n*m*t+1:n*m*t+n) < 0.1));
while abs(fval-prev) > 0.01
    prev = fval;
    f = horzcat(0.01 * t./(x(1:n*m*t)'+0.001), 1./(x(n*m*t+1:n*m*t+n)'+0.001), 0.00000001./(x(n*m*t+n+1:n*m*t+n+n*m*(t-1))'+0.001));
    [x, fval] = linprog(f,A,B,[],[],lb,ub,options);
    k = k+1;
    fprintf("%d: %f y=%d\n", k, fval, sum(x(n*m*t+1:n*m*t+n) < 0.1));
end