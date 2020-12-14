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
f = horzcat(0.001*ones(1, n * dn), ones(1, n));
A = zeros(dn + n, n * dn + n);
B = horzcat(ones(1, dn), -c);
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
    A(dn+k, :) = horzcat(reshape(tmp',1,[]),tmp2);
end
lb = zeros(1, n * dn + n);
ub = horzcat(ones(1, n * dn),c);
[x, fval] = linprog(f,A,B,[],[],lb,ub);
y = x(n*dn+1:n*dn+n);
x = reshape(x(1:n*dn), [n,dn])';
[argvalue, argmax] = max(x, [], 1);