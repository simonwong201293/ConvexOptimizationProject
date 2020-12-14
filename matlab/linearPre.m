fname = 'totalserver_1_trial_0.txt';
val = jsondecode(fileread(fname));
n = val.config.totalJobs;
m = val.config.totalServers;
dn = val.config.totalDuration;
wn = val.config.maxWorkload;
a = zeros(1, n);
d = zeros(1, n);
c = zeros(1, n);
for k = 1:dn
    a(k) = val.jobs(k).arrivalTime+1;
    d(k) = val.jobs(k).deadline+1;
    c(k) = val.jobs(k).workload;
end
A = zeros(dn + n + 2 * n * (dn-1), 2 * n * dn);
B = horzcat(ones(1, dn), -c, zeros(1, 2 * n * (dn-1)));
tmp = zeros(dn, n);
for k = 1:dn
    for l = 1:dn
        if k==l
            tmp(l,:) = ones(1, n);
        else
            tmp(l,:) = zeros(1, n);
        end
    end
    A(k, 1:n*dn) = reshape(tmp,1,[]);
end
for k = 1:n
    tmp = zeros(n, dn);
    for l = 1:dn
        if k==l
            tmp(l,a(k):d(k)) = -ones(1, d(k)-a(k)+1);
        end
    end
    tmp2 = zeros(1, n);
    tmp2(k) = -1;
    A(dn+k, 1:n*dn+n) = horzcat(reshape(tmp',1,[]),tmp2);
end
for k = 0:(n-1)
    for l = 1:dn-1
        A(dn+n+k*(dn-1)+l, k*dn+l) = 1;
        A(dn+n+k*(dn-1)+l, k*dn+l+1) = -1;
        A(dn+n+k*(dn-1)+l, n*dn+n+k*(dn-1)+l) = -1;
        A(dn+n*dn+k*(dn-1)+l, k*dn+l) = -1;
        A(dn+n*dn+k*(dn-1)+l, k*dn+l+1) = 1;
        A(dn+n*dn+k*(dn-1)+l, n*dn+n+k*(dn-1)+l) = -1;
    end
end
lb = zeros(1, 2 * n * dn);
ub = horzcat(ones(1, n * dn), c, ones(1, n * (dn-1)));
options = optimoptions('linprog','Display','none');
f = horzcat(0.01*ones(1, n * dn), 10*ones(1, n), 0.00000001*ones(1, n * (dn-1)));
[x, fval] = linprog(f,A,B,[],[],lb,ub,options);
prev = -1;
k = 0;
fprintf("%d: %f y=%d\n", k, fval, sum(x(n*dn+1:n*dn+n) < 0.1));
while abs(fval-prev) > 0.01
    prev = fval;
    f = horzcat(0.01./(x(1:n * dn)'+0.001), 10./(x(n * dn+1:n * dn+n)'+0.001), 0.00000001./(x(n * dn+n+1: 2 *n * dn)'+0.001));
    [x, fval] = linprog(f,A,B,[],[],lb,ub,options);
    k = k+1;
    fprintf("%d: %f y=%d\n", k, fval, sum(x(n*dn+1:n*dn+n) < 0.1));
end
y = x(n*dn+1:n*dn+n);
z = x(n*dn+n+1:2*n*dn);
x = reshape(x(1:n*dn), [n,dn])';
[argvalue, argmax] = max(x, [], 1);