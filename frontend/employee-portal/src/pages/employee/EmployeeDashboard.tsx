import { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  PolarAngleAxis,
  RadialBar,
  RadialBarChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { Users, LayoutGrid, CheckCircle, AlertCircle, Loader2 } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import Header from "@/components/Header";
import { useEmployeeAuth } from "@/hooks/useEmployeeAuth";
import { stallApi, type Stall } from "@/lib/stallApi";

const RESERVED_COLOR = "hsl(var(--employee))";
const AVAILABLE_COLOR = "hsl(var(--primary))";
const MUTED_COLOR = "hsl(var(--muted))";

export default function EmployeeDashboard() {
  const { isAuthenticated } = useEmployeeAuth();
  const stallsQuery = useQuery({ queryKey: ["all-stalls"], queryFn: stallApi.listStalls });
  const eventsQuery = useQuery({ queryKey: ["dashboard-events"], queryFn: stallApi.listEvents });
  const [selectedEventId, setSelectedEventId] = useState<string>("ALL");

  const stalls = stallsQuery.data ?? [];
  const events = eventsQuery.data ?? [];
  const isLoading = stallsQuery.isLoading || eventsQuery.isLoading;
  const selectedEvent = selectedEventId === "ALL" ? null : events.find((event) => event.id === selectedEventId) ?? null;
  const filteredStalls = useMemo(() => {
    if (!selectedEvent) return stalls;
    return stalls.filter((stall) => stall.eventId === selectedEvent.id);
  }, [stalls, selectedEvent]);

  useEffect(() => {
    if (selectedEventId === "ALL") return;
    if (!events.some((event) => event.id === selectedEventId)) {
      setSelectedEventId(events.length > 0 ? events[0].id : "ALL");
    }
  }, [events, selectedEventId]);

  const { totalStalls, reservedStalls, availableStalls, sizeStats, pieData } = useMemo(() => {
    const defaultSizeStats: Record<Stall["sizeCategory"], { total: number; reserved: number }> = {
      SMALL: { total: 0, reserved: 0 },
      MEDIUM: { total: 0, reserved: 0 },
      LARGE: { total: 0, reserved: 0 },
    };

    if (filteredStalls.length === 0) {
      return {
        totalStalls: 0,
        reservedStalls: 0,
        availableStalls: 0,
        sizeStats: defaultSizeStats,
        pieData: [
          { name: "Reserved", value: 0, fill: RESERVED_COLOR },
          { name: "Available", value: 0, fill: MUTED_COLOR },
        ],
      } as const;
    }

    const bySize: Record<Stall["sizeCategory"], { total: number; reserved: number }> = {
      SMALL: { total: 0, reserved: 0 },
      MEDIUM: { total: 0, reserved: 0 },
      LARGE: { total: 0, reserved: 0 },
    };

    let reserved = 0;
    filteredStalls.forEach((stall) => {
      bySize[stall.sizeCategory].total += 1;
      if (stall.isReserved) {
        reserved += 1;
        bySize[stall.sizeCategory].reserved += 1;
      }
    });

    const total = filteredStalls.length;
    const available = total - reserved;

    return {
      totalStalls: total,
      reservedStalls: reserved,
      availableStalls: available,
      sizeStats: bySize,
      pieData: [
        { name: "Reserved", value: reserved, fill: RESERVED_COLOR },
        { name: "Available", value: available, fill: MUTED_COLOR },
      ],
    };
  }, [filteredStalls]);

  const occupancyRate = totalStalls ? Math.round((reservedStalls / totalStalls) * 100) : 0;

  const eventOccupancyData = useMemo(() => {
    if (events.length === 0) {
      return [] as Array<{ id: string; name: string; total: number; reserved: number; occupancy: number }>;
    }

    const sourceStalls = selectedEvent ? filteredStalls : stalls;
    const eventMap = events.reduce(
      (acc, event) => {
        acc[event.id] = { id: event.id, name: event.name, total: 0, reserved: 0 };
        return acc;
      },
      {} as Record<string, { id: string; name: string; total: number; reserved: number }>,
    );

    sourceStalls.forEach((stall) => {
      const entry = eventMap[stall.eventId] ?? {
        id: stall.eventId,
        name: stall.eventName ?? `Event ${stall.eventId.slice(0, 4)}`,
        total: 0,
        reserved: 0,
      };
      entry.total += 1;
      if (stall.isReserved) {
        entry.reserved += 1;
      }
      eventMap[stall.eventId] = entry;
    });

    const rawData = Object.values(eventMap).map((entry) => ({
      ...entry,
      occupancy: entry.total ? Math.round((entry.reserved / entry.total) * 100) : 0,
    }));

    if (selectedEvent) {
      const match = rawData.find((entry) => entry.id === selectedEvent.id);
      return match && match.total > 0 ? [match] : [];
    }

    return rawData
      .filter((entry) => entry.total > 0)
      .sort((a, b) => b.total - a.total)
      .slice(0, 5);
  }, [events, stalls, filteredStalls, selectedEvent]);

  const chartData = [
    {
      name: "Small",
      reserved: sizeStats.SMALL.reserved,
      available: sizeStats.SMALL.total - sizeStats.SMALL.reserved,
    },
    {
      name: "Medium",
      reserved: sizeStats.MEDIUM.reserved,
      available: sizeStats.MEDIUM.total - sizeStats.MEDIUM.reserved,
    },
    {
      name: "Large",
      reserved: sizeStats.LARGE.reserved,
      available: sizeStats.LARGE.total - sizeStats.LARGE.reserved,
    },
  ];
  const contextLabel = selectedEvent ? selectedEvent.name : "All events";

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-background">
      <Header isEmployee />
      
      <div className="container py-8">
        <div className="space-y-8">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <h1 className="text-4xl font-bold mb-2">Organiser Dashboard</h1>
              <p className="text-muted-foreground">
                {selectedEvent
                  ? `Live stall metrics for ${selectedEvent.name}`
                  : `Overview across ${events.length} event${events.length === 1 ? "" : "s"}`}
              </p>
            </div>
            <div className="w-full lg:w-72 space-y-2">
              <p className="text-sm font-medium text-muted-foreground">Event</p>
              <Select
                value={selectedEvent ? selectedEvent.id : "ALL"}
                onValueChange={(value) => setSelectedEventId(value)}
                disabled={eventsQuery.isLoading}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select event" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All events</SelectItem>
                  {events.map((event) => (
                    <SelectItem key={event.id} value={event.id}>
                      {event.name} ({event.year})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {selectedEvent && (
                <p className="text-xs text-muted-foreground">
                  {selectedEvent.location} | {selectedEvent.startDate} - {selectedEvent.endDate}
                </p>
              )}
            </div>
          </div>

          {isLoading && (
            <div className="flex items-center gap-2 text-muted-foreground">
              <Loader2 className="h-4 w-4 animate-spin" />
              Loading stall-service data...
            </div>
          )}

          {/* Key Metrics */}
          <div className="grid md:grid-cols-4 gap-4">
            <Card>
              <CardHeader className="pb-2">
                <CardDescription>Total Stalls ({contextLabel})</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <span className="text-3xl font-bold">{totalStalls}</span>
                  <LayoutGrid className="h-8 w-8 text-muted-foreground" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-2">
                <CardDescription>Reserved ({contextLabel})</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <span className="text-3xl font-bold text-employee">{reservedStalls}</span>
                  <CheckCircle className="h-8 w-8 text-employee" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-2">
                <CardDescription>Available ({contextLabel})</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <span className="text-3xl font-bold text-primary">{availableStalls}</span>
                  <AlertCircle className="h-8 w-8 text-primary" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-2">
                <CardDescription>Occupancy Rate ({contextLabel})</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <span className="text-3xl font-bold">{occupancyRate}%</span>
                  <Users className="h-8 w-8 text-muted-foreground" />
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Charts */}
          <div className="grid md:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle>Occupancy by Stall Size</CardTitle>
                <CardDescription>Reserved vs available inventory for {contextLabel}</CardDescription>
              </CardHeader>
              <CardContent>
                <ResponsiveContainer width="100%" height={320}>
                  <BarChart data={chartData} barCategoryGap={24}>
                    <defs>
                      <linearGradient id="reservedGradient" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor={RESERVED_COLOR} stopOpacity={0.9} />
                        <stop offset="100%" stopColor={RESERVED_COLOR} stopOpacity={0.6} />
                      </linearGradient>
                      <linearGradient id="availableGradient" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor={AVAILABLE_COLOR} stopOpacity={0.9} />
                        <stop offset="100%" stopColor={AVAILABLE_COLOR} stopOpacity={0.5} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid vertical={false} strokeDasharray="4 8" stroke="hsl(var(--border))" />
                    <XAxis dataKey="name" tickLine={false} axisLine={false} />
                    <YAxis allowDecimals={false} tickLine={false} axisLine={false} />
                    <Tooltip content={<DashboardTooltip />} cursor={{ fill: "hsl(var(--muted))", opacity: 0.15 }} />
                    <Bar
                      dataKey="reserved"
                      fill="url(#reservedGradient)"
                      name="Reserved"
                      stackId="sizes"
                      radius={[8, 8, 0, 0]}
                    />
                    <Bar
                      dataKey="available"
                      fill="url(#availableGradient)"
                      name="Available"
                      stackId="sizes"
                      radius={[8, 8, 0, 0]}
                    />
                  </BarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Overall Occupancy</CardTitle>
                <CardDescription>Live share of reserved vs open stalls for {contextLabel}</CardDescription>
              </CardHeader>
              <CardContent className="relative h-[320px]">
                <ResponsiveContainer width="100%" height="100%">
                  <RadialBarChart
                    data={pieData}
                    innerRadius="45%"
                    outerRadius="95%"
                    startAngle={90}
                    endAngle={-270}
                  >
                    <PolarAngleAxis type="number" domain={[0, Math.max(totalStalls, 1)]} tick={false} />
                    <RadialBar
                      dataKey="value"
                      background
                      clockWise
                      cornerRadius={16}
                      fill={RESERVED_COLOR}
                    />
                    <Tooltip content={<DashboardTooltip />} />
                  </RadialBarChart>
                </ResponsiveContainer>
                <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center text-center">
                  <span className="text-4xl font-bold">{occupancyRate}%</span>
                  <span className="text-sm text-muted-foreground">Occupancy</span>
                </div>
                <div className="mt-4 flex justify-center gap-6 text-sm">
                  <div className="flex items-center gap-2">
                    <span className="h-3 w-3 rounded-full" style={{ background: RESERVED_COLOR }} />
                    <span className="text-muted-foreground">Reserved {reservedStalls}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="h-3 w-3 rounded-full" style={{ background: MUTED_COLOR }} />
                    <span className="text-muted-foreground">Available {availableStalls}</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>
                {selectedEvent ? `${selectedEvent.name} Occupancy` : "Top Events by Occupancy"}
              </CardTitle>
              <CardDescription>
                {selectedEvent
                  ? "Reserved vs total stalls for the selected event"
                  : "Focus on the busiest fairs right now"}
              </CardDescription>
            </CardHeader>
            <CardContent>
              {eventOccupancyData.length === 0 ? (
                <p className="text-muted-foreground text-sm">
                  {selectedEvent
                    ? `No stall data available for ${selectedEvent.name} yet.`
                    : "No events found yet."}
                </p>
              ) : (
                <ResponsiveContainer width="100%" height={320}>
                  <AreaChart data={eventOccupancyData} margin={{ left: 0, right: 0, top: 16, bottom: 0 }}>
                    <defs>
                      <linearGradient id="occupancyGradient" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="0%" stopColor={RESERVED_COLOR} stopOpacity={0.35} />
                        <stop offset="95%" stopColor={RESERVED_COLOR} stopOpacity={0.05} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="hsl(var(--border))" />
                    <XAxis dataKey="name" tickLine={false} axisLine={false} interval={0} tick={{ fontSize: 12 }} />
                    <YAxis
                      domain={[0, 100]}
                      tickFormatter={(value) => `${value}%`}
                      tickLine={false}
                      axisLine={false}
                    />
                    <Tooltip content={<DashboardTooltip suffix="%" />} />
                    <Area
                      type="monotone"
                      dataKey="occupancy"
                      stroke={RESERVED_COLOR}
                      strokeWidth={3}
                      fill="url(#occupancyGradient)"
                      dot={{ strokeWidth: 2, r: 4 }}
                    />
                  </AreaChart>
                </ResponsiveContainer>
              )}
            </CardContent>
          </Card>

          {/* Stall Size Breakdown */}
          <Card>
            <CardHeader>
              <CardTitle>Detailed Breakdown by Size</CardTitle>
              <CardDescription>Inventory snapshot for {contextLabel}</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="grid grid-cols-3 gap-4 text-sm font-medium text-muted-foreground">
                  <div>Stall Size</div>
                  <div className="text-center">Reserved</div>
                  <div className="text-center">Available</div>
                </div>
                
                <div className="grid grid-cols-3 gap-4 items-center">
                  <div className="font-semibold">Small (3m x 2m)</div>
                  <div className="text-center text-lg font-bold text-employee">{sizeStats.SMALL.reserved}</div>
                  <div className="text-center text-lg font-bold">{sizeStats.SMALL.total - sizeStats.SMALL.reserved}</div>
                </div>
                
                <div className="grid grid-cols-3 gap-4 items-center">
                  <div className="font-semibold">Medium (4m x 3m)</div>
                  <div className="text-center text-lg font-bold text-employee">{sizeStats.MEDIUM.reserved}</div>
                  <div className="text-center text-lg font-bold">{sizeStats.MEDIUM.total - sizeStats.MEDIUM.reserved}</div>
                </div>
                
                <div className="grid grid-cols-3 gap-4 items-center">
                  <div className="font-semibold">Large (6m x 4m)</div>
                  <div className="text-center text-lg font-bold text-employee">{sizeStats.LARGE.reserved}</div>
                  <div className="text-center text-lg font-bold">{sizeStats.LARGE.total - sizeStats.LARGE.reserved}</div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}

type TooltipPayloadItem = {
  name?: string;
  value?: number;
  color?: string;
};

interface DashboardTooltipProps {
  active?: boolean;
  payload?: TooltipPayloadItem[];
  label?: string;
  suffix?: string;
}

function DashboardTooltip({ active, payload, label, suffix = "" }: DashboardTooltipProps) {
  if (!active || !payload || payload.length === 0) {
    return null;
  }

  return (
    <div className="rounded-md border bg-card px-3 py-2 text-xs shadow-lg">
      {label && <p className="mb-1 font-semibold">{label}</p>}
      <div className="space-y-1">
        {payload.map((entry) => (
          <div key={entry.name} className="flex items-center justify-between gap-4">
            <span className="text-muted-foreground">{entry.name}</span>
            <span className="font-semibold">
              {entry.value}
              {suffix}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}
