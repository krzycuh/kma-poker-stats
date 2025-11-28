interface PlacementBadgeProps {
  placement: number;
}

export const PlacementBadge = ({ placement }: PlacementBadgeProps) => {
  // Only show badges for top 3 placements
  if (placement > 3) return null;

  const getBadgeConfig = (placement: number) => {
    switch (placement) {
      case 1:
        return {
          emoji: '🥇',
          bgColor: 'bg-yellow-100',
          textColor: 'text-yellow-800',
          borderColor: 'border-yellow-300',
          label: '1st',
        };
      case 2:
        return {
          emoji: '🥈',
          bgColor: 'bg-gray-100',
          textColor: 'text-gray-700',
          borderColor: 'border-gray-300',
          label: '2nd',
        };
      case 3:
        return {
          emoji: '🥉',
          bgColor: 'bg-orange-100',
          textColor: 'text-orange-800',
          borderColor: 'border-orange-300',
          label: '3rd',
        };
      default:
        return null;
    }
  };

  const config = getBadgeConfig(placement);
  if (!config) return null;

  return (
    <span
      className={`inline-flex items-center gap-1 px-2 py-1 rounded-md border ${config.bgColor} ${config.textColor} ${config.borderColor} text-xs font-medium`}
      title={`${config.label} Place`}
    >
      <span className="text-sm">{config.emoji}</span>
      <span>{config.label}</span>
    </span>
  );
};
